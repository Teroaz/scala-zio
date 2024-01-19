package escalade

case class EnvVars(
  dataUrl: String,
  startYear: Int,
  endYear: Int,
  csvSeparator: Option[String],
)
