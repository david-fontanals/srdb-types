package com.agilogy.srdb.test

import java.sql._
import org.scalamock.scalatest.MockFactory
import org.scalatest.FlatSpec

class OptionalColumnDbTypesTest extends FlatSpec with MockFactory {

  val ps = mock[PreparedStatement]
  val rs = mock[ResultSet]
  val db = new DummyDb(ps, rs)

  behavior of "optional DbType implicit conversion"

  import com.agilogy.srdb.types._
  import DbType._

  it should "set null parameters using None values of type Option[T]" in {
    inSequence {
      (ps.setNull(_:Int , _:Int)).expects(1, Types.INTEGER)
      (rs.getInt(_: Int)).expects(1).returning(3)
      (rs.wasNull _).expects().returning(false)
    }
    val optInt:Option[Int] = None
    db.prepare(optInt)
    db.read(reader[Int])
  }

  it should "read null parameters as None of type Option[T]" in {
    inSequence {
      (ps.setInt(_, _)).expects(1, 3)
      (rs.getInt(_: Int)).expects(1).returning(0)
      (rs.wasNull _).expects().returning(true)
    }
    db.prepare(3)
    assert(db.read(reader[Option[Int]]) === None)
  }


}
