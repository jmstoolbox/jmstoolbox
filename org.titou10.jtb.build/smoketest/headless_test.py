#!/usr/bin/env python3
# Tests a running JMSToolBox through its REST services (started by headless.sh).
# For each session of config.xml: list the destinations, empty the test queue, send a message, browse it, empty the queue.
# Every request has a timeout, so a hanging Q Manager plugin fails the test instead of blocking it.
# Only uses the Python standard library.
import json
import sys
import time
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET

BASE_URL = "http://localhost:9998/rest"
TIMEOUT = 60  # seconds per request

# Test queue created by setup.sh. Oracle AQ doesn't allow dots in queue names
TEST_QUEUE = "JTB.TEST"
TEST_QUEUE_ORACLE = "JTB_TEST"

# Number of queues and of topics created by setup.sh in ActiveMQ Classic for the discovery tests
LOAD_COUNT = 600


def request(method, path, body=None):
    data = None if body is None else json.dumps(body).encode()
    req = urllib.request.Request(BASE_URL + path, data=data, method=method)
    if data is not None:
        req.add_header("Content-Type", "application/json")
    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT) as resp:
            text = resp.read().decode()
            return resp.status, (json.loads(text) if text else None)
    except urllib.error.HTTPError as e:
        raise AssertionError(f"{method} {path} -> HTTP {e.code}: {e.read().decode()[:300]}") from None
    except TimeoutError:
        raise AssertionError(f"{method} {path} -> no answer within {TIMEOUT} s") from None
    except urllib.error.URLError as e:
        raise AssertionError(f"{method} {path} -> {e.reason}") from None


def q(name):
    return urllib.parse.quote(name, safe="")


def test_session(session):
    s = q(session)

    status, destinations = request("GET", f"/session/{s}")
    names = [d["name"] for d in destinations or []]
    queue = next((n for n in names if n == TEST_QUEUE or n.endswith(TEST_QUEUE_ORACLE)), None)
    if queue is None:
        raise AssertionError(f"test queue not found among {len(names)} destinations: {names[:10]}")
    d = q(queue)

    request("DELETE", f"/message/{s}/{d}")

    payload = f"jtb-smoketest-{session}-{time.time_ns()}"
    status, sent = request("POST", f"/message/{s}/{d}", {"type": "TEXT", "payloadText": payload})
    if status != 201:
        raise AssertionError(f"sending returned HTTP {status}")

    # Some brokers make a message visible to browsers only after a short delay
    for _ in range(10):
        status, browsed = request("GET", f"/message/{s}/{d}")
        if any(m.get("payloadText") == payload for m in browsed or []):
            break
        time.sleep(1)
    else:
        raise AssertionError(f"sent message not found when browsing {queue}")

    request("DELETE", f"/message/{s}/{d}")
    status, browsed = request("GET", f"/message/{s}/{d}")
    if browsed:
        raise AssertionError(f"{queue} still contains {len(browsed)} message(s) after emptying it")

    return f"{len(names)} destinations, send/browse/empty OK on {queue}"


def test_discovery(session):
    # setup.sh creates JTB.LOAD.Q.000-599 and JTB.LOAD.T.000-599. All of them must be listed on the first listing
    status, destinations = request("GET", f"/session/{q(session)}")
    names = {d["name"] for d in destinations or []}
    expected = {f"JTB.LOAD.{kind}.{i:03d}" for kind in ("Q", "T") for i in range(LOAD_COUNT)}
    missing = expected - names
    queues = sum(1 for n in names if n.startswith("JTB.LOAD.Q."))
    topics = sum(1 for n in names if n.startswith("JTB.LOAD.T."))
    if missing:
        raise AssertionError(f"listed {queues} of {LOAD_COUNT} queues and {topics} of {LOAD_COUNT} topics "
                             f"({len(missing)} missing, e.g. {sorted(missing)[:3]})")
    return f"all {LOAD_COUNT} queues and {LOAD_COUNT} topics listed"


def main():
    config = sys.argv[1] if len(sys.argv) > 1 else "config.xml"
    sessions = [s.get("name") for s in ET.parse(config).getroot().iter("sessionDef")]

    failures = 0
    for session in sessions:
        start = time.time()
        try:
            test = test_discovery if session.startswith("Discovery") else test_session
            result, ok = test(session), True
        except AssertionError as e:
            result, ok = str(e), False
            failures += 1
        print(f"{'PASS' if ok else 'FAIL'}  {session:<20} {time.time() - start:5.1f} s  {result}", flush=True)

    print(f"\n{len(sessions) - failures} of {len(sessions)} sessions passed")
    sys.exit(1 if failures else 0)


if __name__ == "__main__":
    main()
