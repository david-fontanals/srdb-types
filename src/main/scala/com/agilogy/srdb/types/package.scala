package com.agilogy.srdb

import java.sql.{ ResultSet, PreparedStatement }

import scala.language.implicitConversions

package object types extends ColumnTypeInstances with DbTypeCombinators {

  /**
   * Exposes an implicit [[NotNullDbType]] for each [[ColumnType]] implicitly available
   * @tparam T The Scala type for which the [[NotNullDbType]] is to be exposed
   * @group API
   */
  implicit def notNullView[T: ColumnType]: NotNullDbType[T] = new NotNullDbType[T] {

    private val columnType = implicitly[ColumnType[T]]

    override val length: Int = 1

    override def get(rs: ResultSet, pos: Int): T = columnType.get(rs, pos).getOrElse(throw new NullColumnReadException)

    override def set(ps: PreparedStatement, pos: Int, value: T): Unit = columnType.set(ps, pos, Some(value))

  }

  /**
   * Exposes an implicit [[OptionalDbType]] for each [[ColumnType]] implicitly available
   * @tparam T The Scala type for which the [[OptionalDbType]] is to be exposed
   * @group API
   */
  implicit def optionalView[T: ColumnType]: OptionalDbType[T] = new OptionalDbType[T] {

    private val columnType = implicitly[ColumnType[T]]

    override val length: Int = 1

    override def get(rs: ResultSet, pos: Int): Option[T] = columnType.get(rs, pos)

    override def set(ps: PreparedStatement, pos: Int, value: Option[T]): Unit = columnType.set(ps, pos, value)

    override def notNull: NotNullDbType[T] = notNullView[T]
  }

  /**
   * Returns a [[NotNullNamedDbReader]] that reads a column with the given name using the implicit [[ColumnType]] for `T`
   * @tparam T The Scala type for which the [[NotNullNamedDbReader]] is to be returned
   * @group API
   */
  def notNull[T: ColumnType](name: String): NotNullNamedDbReader[T] = new NotNullNamedDbReader[T] {

    override def get(rs: ResultSet): T = implicitly[ColumnType[T]].get(rs, name).getOrElse(throw new NullColumnReadException)

  }

  /**
   * Returns an [[OptionalNamedDbReader]] that reads a column with the given name using the implicit [[ColumnType]] for `T`
   * @tparam T The Scala type for which the [[OptionalNamedDbReader]] is to be returned
   * @group API
   */
  def optional[T: ColumnType](name: String): OptionalNamedDbReader[T] = notNull[T](name).optional

  /**
   * Sets a `T` parameter in a `PreparedStatement` using a [[DbWriter]] (or [[DbType]]).
   *
   * Equivalent to `set(ps,1,value)`.
   *
   * Depending on the [[DbWriter]] being used, one or more parameters of the `PreparedStatement` will be set.
   *
   * @param ps The `PreparedStatement`
   * @param value The value to set
   * @tparam T The Scala type of the value to be set
   * @group API
   */
  def set[T: DbWriter](ps: PreparedStatement, value: T): Unit = implicitly[DbWriter[T]].set(ps, value)

  /**
   * Sets a `T` parameter in a `PreparedStatement`, starting at `pos` using a [[DbWriter]] (or [[DbType]]).
   *
   * As in JDBC, `pos` is 1-based, so 1 is the first parameter
   * Depending on the [[DbWriter]] being used, one or more parameters of the `PreparedStatement` will be set. If so,
   * the parameters are set starting at parameter `pos`.
   *
   * @param ps The `PreparedStatement`
   * @param pos The parameter to start at
   * @param value The value to set
   * @tparam T The Scala type of the value to be set
   * @group API
   */
  def set[T: DbWriter](ps: PreparedStatement, pos: Int, value: T): Unit = implicitly[DbWriter[T]].set(ps, pos, value)

  /**
   * Reads a value from a `ResultSet` using the given [[DbReader]] (or [[DbType]]), which may read the column(s) by position or by name
   * @param rs The `ResultSet` to get the value from
   * @tparam T The Scala type of the read value
   * @return The Scala representation of one or more columns according to the [[DbReader]] used
   * @group API
   */
  def get[T: DbReader](rs: ResultSet): T = implicitly[DbReader[T]].get(rs)

  /** @group API */
  implicit def argument[T: DbType](v: T): (PreparedStatement, Int) => Unit = {
    (ps, pos) =>
      implicitly[DbType[T]].set(ps, pos, v)
  }

}
