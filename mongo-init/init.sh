#!/bin/bash
set -e

wait_for() {
  local host=$1
  local port=$2
  echo "Waiting for $host:$port..."
  until mongosh --host "$host" --port "$port" --eval "db.adminCommand('ping')" --quiet 2>/dev/null; do
    sleep 2
  done
  echo "$host:$port is ready"
}

wait_for configsvr 27019
mongosh --host configsvr --port 27019 /scripts/init-configsvr.js

wait_for shard1a 27018
mongosh --host shard1a --port 27018 /scripts/init-shard1.js

wait_for shard2a 27020
mongosh --host shard2a --port 27020 /scripts/init-shard2.js

# Give replica sets time to elect a primary
sleep 15

wait_for mongos 27017
mongosh --host mongos --port 27017 /scripts/cluster-init.js

echo "Cluster initialization complete"
