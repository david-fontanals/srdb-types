package com.agilogy.srdb.test

import java.sql._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ Resources, FlatSpec }

import scala.reflect.Manifest
import scala.util.control.NonFatal

class AtomicDbTypesNotNullsTest extends FlatSpec with MockFactory {

  val conn: Connection = mock[Connection]
  val ps: PreparedStatement = mock[PreparedStatement]
  val rs: ResultSet = mock[ResultSet]

  import com.agilogy.srdb.types._

  behavior of "not null ColumnType implicit conversions"

  it should "prepare statements with a Byte param and read resultsets with a Byte column" in {
    inSequence {
      (ps.setByte(_, _)).expects(1, 3.toByte)
      (rs.getByte(_: Int)).expects(1).returning(3.toByte)
      (rs.wasNull _).expects().returning(false)
      (rs.getByte(_: String)).expects("c").returning(3.toByte)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, 3.toByte)
    assert(get[Byte](rs) === 3.toByte)
    assert(get(rs)(notNull[Byte]("c")) === 3.toByte)
  }

  it should "prepare statements with a Short param and read resultsets with a Short column" in {
    inSequence {
      (ps.setShort(_, _)).expects(1, 3.toShort)
      (rs.getShort(_: Int)).expects(1).returning(3.toShort)
      (rs.wasNull _).expects().returning(false)
      (rs.getShort(_: String)).expects("c").returning(3.toShort)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, 3.toShort)
    assert(get[Short](rs) === 3.toShort)
    assert(get(rs)(notNull[Short]("c")) === 3.toShort)
  }

  it should "execute selects with an Int param" in {
    inSequence {
      (ps.setInt(_, _)).expects(1, 3)
      (rs.getInt(_: Int)).expects(1).returning(3)
      (rs.wasNull _).expects().returning(false)
      (rs.getInt(_: String)).expects("c").returning(3)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, 3)
    assert(get[Int](rs) === 3)
    assert(get(rs)(notNull[Int]("c")) === 3)
  }

  it should "prepare statements with a Long param and read resultsets with a Long column" in {
    inSequence {
      (ps.setLong(_, _)).expects(1, 3l)
      (rs.getLong(_: Int)).expects(1).returning(3l)
      (rs.wasNull _).expects().returning(false)
      (rs.getLong(_: String)).expects("c").returning(3l)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, 3l)
    assert(get[Long](rs) === 3l)
    assert(get(rs)(notNull[Long]("c")) === 3l)
  }

  it should "prepare statements with a Float param and read resultsets with a Float column" in {
    inSequence {
      (ps.setFloat _).expects(1, 3.0f)
      (rs.getFloat(_: Int)).expects(1).returning(3.0f)
      (rs.wasNull _).expects().returning(false)
      (rs.getFloat(_: String)).expects("c").returning(3.0f)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, 3.0f)
    assert(get[Float](rs) === 3.0f)
    assert(get(rs)(notNull[Float]("c")) === 3.0f)
  }

  it should "prepare statements with a Double param and read resultsets with a Double column" in {
    inSequence {
      (ps.setDouble _).expects(1, 3.0)
      (rs.getDouble(_: Int)).expects(1).returning(3.0)
      (rs.wasNull _).expects().returning(false)
      (rs.getDouble(_: String)).expects("c").returning(3.0)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, 3.0)
    assert(get[Double](rs) === 3.0)
    assert(get(rs)(notNull[Double]("c")) === 3.0)
  }

  it should "prepare statements with a String param and read resultsets with a String column" in {
    inSequence {
      (ps.setString _).expects(1, "hi!")
      (rs.getString(_: Int)).expects(1).returning("hi!")
      (rs.wasNull _).expects().returning(false)
      (rs.getString(_: String)).expects("c").returning("hi!")
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, "hi!")
    assert(get[String](rs) === "hi!")
    assert(get(rs)(notNull[String]("c")) === "hi!")
  }

  it should "prepare statements with a Boolean param and read resultsets with a Boolean column" in {
    inSequence {
      (ps.setBoolean _).expects(1, false)
      (rs.getBoolean(_: Int)).expects(1).returning(false)
      (rs.wasNull _).expects().returning(false)
      (rs.getBoolean(_: String)).expects("c").returning(false)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, false)
    assert(get[Boolean](rs) === false)
    assert(get(rs)(notNull[Boolean]("c")) === false)
  }

  it should "prepare statements with a java.util.Date param and read resultsets with a java.util.Date column" in {
    val d = new java.util.Date(123456l)
    val sqlDate = new java.sql.Date(d.getTime)
    inSequence {
      (ps.setDate(_: Int, _: java.sql.Date)).expects(1, sqlDate)
      (rs.getDate(_: Int)).expects(1).returning(sqlDate)
      (rs.wasNull _).expects().returning(false)
      (rs.getDate(_: String)).expects("c").returning(sqlDate)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, d)
    assert(get[java.util.Date](rs) === d)
    assert(get(rs)(notNull[java.util.Date]("c")) === d)
  }

  it should "prepare statements with a BigDecimal param and read resultsets with a BigDecimal column" in {
    val value = BigDecimal("2.0")
    val javaBd = value.bigDecimal
    inSequence {
      (ps.setBigDecimal(_: Int, _: java.math.BigDecimal)).expects(1, javaBd)
      (rs.getBigDecimal(_: Int)).expects(1).returning(javaBd)
      (rs.wasNull _).expects().returning(false)
      (rs.getBigDecimal(_: String)).expects("c").returning(javaBd)
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, value)
    assert(get[BigDecimal](rs) === value)
    assert(get(rs)(notNull[BigDecimal]("c")) === value)
  }

  it should "prepare statements with an array param and read resultsets with an array column" in {
    val value = Seq(1, 2)
    val arr = mock[java.sql.Array]
    implicit val intArrayDbType = arrayDbType[Int]("int")
    val arrRs = mock[ResultSet]
    def expectReadArray() = {
      (() => arr.getResultSet).expects().returning(arrRs)
      (arrRs.next _).expects().returning(true)
      (arrRs.getInt(_: Int)).expects(1).returning(1)
      (arrRs.wasNull _).expects().returning(false)
      (arrRs.next _).expects().returning(true)
      (arrRs.getInt(_: Int)).expects(1).returning(2)
      (arrRs.wasNull _).expects().returning(false)
      (arrRs.next _).expects().returning(false)
    }
    inSequence {
      (ps.getConnection _).expects().returning(conn)
      (conn.createArrayOf _).expects(where {
        (typeName: String, elements: scala.Array[AnyRef]) =>
          typeName == "int" && elements.toSeq == value.asInstanceOf[Seq[AnyRef]]
      }).returning(arr)
      (ps.setArray _).expects(1, arr)
      (rs.getArray(_: Int)).expects(1).returning(arr)
      expectReadArray()
      (rs.wasNull _).expects().returning(false)
      (rs.getArray(_: String)).expects("c").returning(arr)
      expectReadArray()
      (rs.wasNull _).expects().returning(false)
    }
    set(ps, value)
    assert(get[Seq[Int]](rs) === value)
    assert(get(rs)(notNull[Seq[Int]]("c")) === value)
  }

  def checkException[T <: Throwable](f: => Any)(implicit manifest: Manifest[T]): Unit = {
    val clazz = manifest.runtimeClass.asInstanceOf[Class[T]]
    try {
      f
      fail("Should have thrown a exception")
    } catch {
      case NonFatal(t) if clazz.isAssignableFrom(t.getClass) => ()
    }
  }

  it should "prepare a statement in a given position" in {
    inSequence {
      (ps.setString _).expects(3, "hey!")
    }
    set(ps, 3, "hey!")
  }

  it should "throw if it reads a null value" in {
    inSequence {
      (rs.getString(_: Int)).expects(1).returning(null)
      (rs.wasNull _).expects().returning(true)
      (rs.getString(_: String)).expects("c").returning(null)
      (rs.wasNull _).expects().returning(true)
    }
    intercept[NullColumnReadException](get[String](rs))
    checkException[NullColumnReadException](get(rs)(notNull[String]("c")))
  }

}
