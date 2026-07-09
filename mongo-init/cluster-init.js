const adminDb = db.getSiblingDB("admin")

if (adminDb.system.users.countDocuments({ user: "admin" }) === 0) {
  adminDb.createUser({
    user: "admin",
    pwd: "admin",
    roles: [{ role: "root", db: "admin" }]
  })
  print("User admin created")
} else {
  print("User admin already exists")
}

try {
  sh.addShard("shard1ReplSet/shard1a:27018,shard1b:27018,shard1c:27018")
} catch (e) {
  print(e.message)
}

try {
  sh.addShard("shard2ReplSet/shard2a:27020,shard2b:27020,shard2c:27020")
} catch (e) {
  print(e.message)
}

try {
  sh.enableSharding("booklibre")
} catch (e) {
  print(e.message)
}

try {
  sh.shardCollection("booklibre.libros", { _id: "hashed" })
} catch (e) {
  print(e.message)
}
