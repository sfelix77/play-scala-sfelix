package models

/**
  *
  * @param ident
  * @param latitudeDegree
  * @param longitudeDegree
  * @param elevationFeet
  * @param headingDegT
  * @param displacedThreshold
  */
case class RunwayEnd (
          ident: Option[String],
          latitudeDegree: Option[BigDecimal],
          longitudeDegree: Option[BigDecimal],
          elevationFeet: Option[Int],
          headingDegT: Option[BigDecimal],
          displacedThreshold: Option[Int]
)

object RunwayEnd { }
