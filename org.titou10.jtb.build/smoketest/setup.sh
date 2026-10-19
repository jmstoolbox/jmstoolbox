#!/usr/bin/env bash
# Starts the brokers of docker-compose.yml and creates the queue JTB.TEST (JTB_TEST in Oracle AQ) in each of them.
# Safe to run again: existing queues are left alone.
set -euo pipefail
cd "$(dirname "$0")"

docker compose up -d --wait

echo "ActiveMQ Classic: creating queue JTB.TEST"
curl -s -o /dev/null -u admin:admin -H 'Origin: http://localhost' -H 'Content-Type: application/json' \
   -X POST http://localhost:8162/api/jolokia/ \
   -d '{"type":"exec","mbean":"org.apache.activemq:type=Broker,brokerName=localhost","operation":"addQueue","arguments":["JTB.TEST"]}'

echo "ActiveMQ Classic: creating 600 queues and 600 topics JTB.LOAD.* for the discovery tests"
python3 -c '
import json
print(json.dumps([{"type": "exec", "mbean": "org.apache.activemq:type=Broker,brokerName=localhost",
                   "operation": op, "arguments": ["JTB.LOAD.%s.%03d" % (kind, i)]}
                  for op, kind in (("addQueue", "Q"), ("addTopic", "T")) for i in range(600)]))' |
curl -s -o /dev/null -u admin:admin -H 'Origin: http://localhost' -H 'Content-Type: application/json' \
   -X POST http://localhost:8162/api/jolokia/ --data-binary @-

echo "Toxiproxy: ActiveMQ Classic with 50 ms +/- 30 ms latency on port 61619"
curl -s -o /dev/null -X POST http://localhost:8474/proxies \
   -d '{"name":"activemq","listen":"0.0.0.0:61619","upstream":"activemq:61616","enabled":true}'
curl -s -o /dev/null -X POST http://localhost:8474/proxies/activemq/toxics \
   -d '{"name":"latency","type":"latency","stream":"downstream","attributes":{"latency":50,"jitter":30}}'

echo "Artemis 2.x: creating queue JTB.TEST"
docker compose exec -T artemis /var/lib/artemis-instance/bin/artemis queue create \
   --name JTB.TEST --address JTB.TEST --anycast --durable --auto-create-address --preserve-on-no-consumers \
   --user admin --password admin --silent >/dev/null 2>&1 || echo "  (already exists)"

echo "Artemis 1.x: creating queue JTB.TEST"
docker compose exec -T artemis1 /var/lib/artemis/bin/artemis destination create \
   --name JTB.TEST --type jms-queue --durable --user admin --password admin --url tcp://localhost:61616 \
   >/dev/null 2>&1 || echo "  (already exists)"

echo "Solace: creating queue JTB.TEST"
curl -s -o /dev/null -u admin:admin -H 'Content-Type: application/json' \
   -X POST http://localhost:8088/SEMP/v2/config/msgVpns/default/queues \
   -d '{"queueName":"JTB.TEST","accessType":"exclusive","permission":"delete","ingressEnabled":true,"egressEnabled":true}'

echo "Oracle AQ: creating queue JTB_TEST"
docker compose exec -T oracle sqlplus -s sys/oracle@localhost/FREEPDB1 as sysdba >/dev/null <<'EOF'
GRANT AQ_ADMINISTRATOR_ROLE, AQ_USER_ROLE, SELECT ANY DICTIONARY TO jtb;
GRANT EXECUTE ON DBMS_AQ TO jtb;
GRANT EXECUTE ON DBMS_AQADM TO jtb;
EOF
docker compose exec -T oracle sqlplus -s jtb/jtb@localhost/FREEPDB1 >/dev/null <<'EOF'
BEGIN
  DBMS_AQADM.CREATE_QUEUE_TABLE(queue_table => 'JTB_QT', queue_payload_type => 'SYS.AQ$_JMS_MESSAGE');
  DBMS_AQADM.CREATE_QUEUE(queue_name => 'JTB_TEST', queue_table => 'JTB_QT');
  DBMS_AQADM.START_QUEUE(queue_name => 'JTB_TEST');
EXCEPTION
  WHEN OTHERS THEN NULL; -- already exists
END;
/
EOF

echo "Brokers ready. Start JMSToolBox with ./run.sh"
