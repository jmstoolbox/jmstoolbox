#!/usr/bin/env bash
# Starts the brokers of docker-compose.yml and creates a test queue in each of them, with a name of its own per broker,
# so that a test can not see the queue of another broker: JTB.TEST.ACTIVEMQ, JTB.TEST.ARTEMIS2, JTB.TEST.ARTEMIS1,
# JTB.TEST.SOLACE and JTB_TEST_ORACLE (Oracle AQ doesn't allow dots in queue names).
# Safe to run again: existing queues are left alone. Any other error stops the script
set -euo pipefail
cd "$(dirname "$0")"

# Runs a command that creates a queue. If it fails, its output must match the pattern of an "already exists" error (if any)
# and the other lines of the output must not contain any error, otherwise the output is shown and the script stops.
# The Artemis CLI exits with 0 even if it fails: its output is checked too
create() {
   local exists=$1 out
   shift
   if out=$("$@" 2>&1) && ! grep -qE 'Failed|Exception' <<<"$out"; then
      return 0
   fi
   if [[ -n "$exists" ]] && grep -qiE "$exists" <<<"$out" \
         && ! grep -viE "$exists" <<<"$out" | grep -qE 'Failed|failed|Exception|Error'; then
      echo "  (already exists)"
      return 0
   fi
   echo "  failed: $out" >&2
   return 1
}

# Posts a JSON request and fails unless the answer is HTTP 200 and contains the given text
post() {
   local url=$1 data=$2 expected=$3 out
   out=$(curl -sS -w '\nHTTP %{http_code}' -u admin:admin -H 'Origin: http://localhost' -H 'Content-Type: application/json' \
            -X POST "$url" -d "$data")
   if [[ "$out" == *"HTTP 200" && "$out" == *"$expected"* ]]; then
      return 0
   fi
   echo "$out"
   return 1
}

# Creates a proxy or a toxic in Toxiproxy: HTTP 200/201 when created, 409 when it already exists
toxiproxy() {
   local path=$1 data=$2 out
   out=$(curl -sS -w '\nHTTP %{http_code}' -X POST "http://localhost:8474$path" -d "$data")
   case "$out" in
      *"HTTP 200" | *"HTTP 201") return 0 ;;
      *"HTTP 409") echo "  (already exists)"; return 0 ;;
   esac
   echo "  failed: $out" >&2
   return 1
}

docker compose up -d --wait

# addQueue does nothing if the queue exists. Jolokia answers HTTP 200 even on errors: its status is in the body
echo "ActiveMQ Classic: creating queue JTB.TEST.ACTIVEMQ"
create '' post http://localhost:8162/api/jolokia/ \
   '{"type":"exec","mbean":"org.apache.activemq:type=Broker,brokerName=localhost","operation":"addQueue","arguments":["JTB.TEST.ACTIVEMQ"]}' \
   '"status":200'

echo "ActiveMQ Classic: creating 600 queues and 600 topics JTB.LOAD.* for the discovery tests"
out=$(python3 -c '
import json
print(json.dumps([{"type": "exec", "mbean": "org.apache.activemq:type=Broker,brokerName=localhost",
                   "operation": op, "arguments": ["JTB.LOAD.%s.%03d" % (kind, i)]}
                  for op, kind in (("addQueue", "Q"), ("addTopic", "T")) for i in range(600)]))' |
   curl -sS -u admin:admin -H 'Origin: http://localhost' -H 'Content-Type: application/json' \
      -X POST http://localhost:8162/api/jolokia/ --data-binary @-)
# One answer per operation, each with its own status (addQueue and addTopic do nothing if the destination exists)
if [[ $(grep -o '"status":200' <<<"$out" | wc -l) -ne 1200 ]]; then
   echo "  failed: ${out:0:2000}" >&2
   exit 1
fi

echo "Toxiproxy: ActiveMQ Classic with 50 ms +/- 30 ms latency on port 61619"
toxiproxy /proxies '{"name":"activemq","listen":"0.0.0.0:61619","upstream":"activemq:61616","enabled":true}'
toxiproxy /proxies/activemq/toxics \
   '{"name":"latency","type":"latency","stream":"downstream","attributes":{"latency":50,"jitter":30}}'

echo "Artemis 2.x: creating queue JTB.TEST.ARTEMIS2"
create 'AMQ229019' docker compose exec -T artemis /var/lib/artemis-instance/bin/artemis queue create \
   --name JTB.TEST.ARTEMIS2 --address JTB.TEST.ARTEMIS2 --anycast --durable --auto-create-address --preserve-on-no-consumers \
   --user admin --password admin --silent </dev/null

# Artemis 1.x doesn't tell why it failed: this message without any other error is the queue that already exists
# (stdin of the Artemis CLI: /dev/null, otherwise it asks for another URL if it can not connect)
echo "Artemis 1.x: creating queue JTB.TEST.ARTEMIS1"
create '^Failed to create jms queue JTB\.TEST\.ARTEMIS1\.$' docker compose exec -T artemis1 /var/lib/artemis/bin/artemis destination create \
   --name JTB.TEST.ARTEMIS1 --type jms-queue --durable --user admin --password admin --url tcp://localhost:61616 </dev/null

echo "Solace: creating queue JTB.TEST.SOLACE"
create 'ALREADY_EXISTS' post http://localhost:8088/SEMP/v2/config/msgVpns/default/queues \
   '{"queueName":"JTB.TEST.SOLACE","accessType":"exclusive","permission":"delete","ingressEnabled":true,"egressEnabled":true}' \
   '"responseCode":200'

# -L: no prompt for another password if the login fails. ORA-24001: queue table exists, ORA-24006: queue exists
echo "Oracle AQ: creating queue JTB_TEST_ORACLE"
create '' docker compose exec -T oracle sqlplus -s -L sys/oracle@localhost/FREEPDB1 as sysdba <<'EOF'
WHENEVER SQLERROR EXIT FAILURE
GRANT AQ_ADMINISTRATOR_ROLE, AQ_USER_ROLE, SELECT ANY DICTIONARY TO jtb;
GRANT EXECUTE ON DBMS_AQ TO jtb;
GRANT EXECUTE ON DBMS_AQADM TO jtb;
EOF
create '' docker compose exec -T oracle sqlplus -s -L jtb/jtb@localhost/FREEPDB1 <<'EOF'
WHENEVER SQLERROR EXIT FAILURE
BEGIN
  BEGIN
    DBMS_AQADM.CREATE_QUEUE_TABLE(queue_table => 'JTB_QT', queue_payload_type => 'SYS.AQ$_JMS_MESSAGE');
  EXCEPTION
    WHEN OTHERS THEN IF SQLCODE != -24001 THEN RAISE; END IF;
  END;
  BEGIN
    DBMS_AQADM.CREATE_QUEUE(queue_name => 'JTB_TEST_ORACLE', queue_table => 'JTB_QT');
  EXCEPTION
    WHEN OTHERS THEN IF SQLCODE != -24006 THEN RAISE; END IF;
  END;
  DBMS_AQADM.START_QUEUE(queue_name => 'JTB_TEST_ORACLE');
END;
/
EOF

echo "Brokers ready. Start JMSToolBox with ./run.sh"
