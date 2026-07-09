try {
  rs.initiate({
    _id: "shard2ReplSet",
    members: [
      { _id: 0, host: "shard2a:27020" },
      { _id: 1, host: "shard2b:27020" },
      { _id: 2, host: "shard2c:27020" }
    ]
  })
} catch (e) {
  if (e.codeName !== "AlreadyInitialized") throw e
  print("shard2ReplSet already initialized")
}
