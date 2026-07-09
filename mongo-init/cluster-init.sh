#!/bin/bash
set -e

mongosh --host mongos --port 27017 --eval "
  use admin
  var existing = db.system.users.countDocuments({ user: '$MONGO_USER' })
  if (existing === 0) {
    db.createUser({
      user: '$MONGO_USER',
      pwd: '$MONGO_PASSWORD',
      roles: [{ role: 'root', db: 'admin' }]
    })
    print('User $MONGO_USER created')
  } else {
    print('User $MONGO_USER already exists, skipping')
  }
  try { sh.addShard('shard1ReplSet/shard1:27018') } catch(e) { print('shard1: ' + e.message) }
  try { sh.addShard('shard2ReplSet/shard2:27020') } catch(e) { print('shard2: ' + e.message) }
  sh.enableSharding('$MONGO_DB')
  try { sh.shardCollection('$MONGO_DB.libros', { _id: 'hashed' }) } catch(e) { print('libros: ' + e.message) }
"
