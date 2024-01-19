package escalade.models

import java.util.Date

case class Transaction(
  date: Date,
  nature: String,
  amount: Double,
  estate: RealEstate
)
