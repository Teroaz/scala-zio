package example

// TODO : Add Validation & constraints with NewTypes & OpaqueTypes
case class EnvVars(
  dataUrl: String,
  startYear: Int,
  endYear: Int,
  csvSeparator: Option[String],
)
