try {
  rs.initiate({
    _id: "configReplSet",
    configsvr: true,
    members: [{ _id: 0, host: "configsvr:27019" }]
  })
} catch (e) {
  if (e.codeName !== "AlreadyInitialized") throw e
  print("configReplSet already initialized")
}
