#!/bin/bash
set -e

mongosh --host configsvr --port 27019 --eval "
  try {
    rs.initiate({ _id: 'configReplSet', configsvr: true, members: [{ _id: 0, host: 'configsvr:27019' }] });
  } catch(e) {
    if (e.codeName !== 'AlreadyInitialized') throw e;
  }
"

mongosh --host shard1 --port 27018 --eval "
  try {
    rs.initiate({ _id: 'shard1ReplSet', members: [{ _id: 0, host: 'shard1:27018' }] });
  } catch(e) {
    if (e.codeName !== 'AlreadyInitialized') throw e;
  }
"

mongosh --host shard2 --port 27020 --eval "
  try {
    rs.initiate({ _id: 'shard2ReplSet', members: [{ _id: 0, host: 'shard2:27020' }] });
  } catch(e) {
    if (e.codeName !== 'AlreadyInitialized') throw e;
  }
"
