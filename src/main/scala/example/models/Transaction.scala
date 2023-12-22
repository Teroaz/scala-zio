package example.models

import java.util.Date

// TODO : Add Validation & constraints with NewTypes & OpaqueTypes
case class Transaction(
  date: Date,
  nature: String,
  amount: Double,
  estate: RealEstate
)
