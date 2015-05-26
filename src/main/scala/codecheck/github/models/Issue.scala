package codecheck.github.models

import org.json4s.JValue
import org.json4s.JString
import org.json4s.JNothing
import org.json4s.JNull
import org.json4s.JArray
import org.json4s.JsonDSL._
import org.joda.time.DateTime

import codecheck.github.utils.ToDo

sealed abstract class IssueState(val name: String) {
  override def toString = name
}

object IssueState {
  case object open extends IssueState("open")
  case object closed extends IssueState("closed")
  case object all extends IssueState("all")

  val values = Array(open, closed, all)

  def fromString(str: String) = values.filter(_.name == str).head
}

sealed abstract class IssueFilter(val name: String) {
  override def toString = name
}

object IssueFilter {
  case object assigned extends IssueFilter("assigned")
  case object created extends IssueFilter("created")
  case object mentioned extends IssueFilter("mentioned")
  case object subscribed extends IssueFilter("subscribed")
  case object all extends IssueFilter("all")

  val values = Array(assigned, created, mentioned, subscribed, all)

  def fromString(str: String) = values.filter(_.name == str).head
}

sealed abstract class IssueSort(val name: String) {
  override def toString = name
}

object IssueSort {
  case object created extends IssueSort("created")
  case object updated extends IssueSort("updated")
  case object comments extends IssueSort("comments")

  val values = Array(created, updated, comments)

  def fromString(str: String) = values.filter(_.name == str).head
}

case class IssueListOption(
  filter: IssueFilter = IssueFilter.assigned,
  state: IssueState = IssueState.open,
  labels: Seq[String] = Nil,
  sort: IssueSort = IssueSort.created,
  direction: SortDirection = SortDirection.desc,
  since: Option[DateTime] = None
) {
  def q = s"?filter=$filter&state=$state&sort=$sort&direction=$direction" +
    (if (labels.length == 0) "" else "&labels=" + labels.mkString(",")) +
    since.map("&since=" + _.toString("yyyy-MM-dd'T'HH:mm:ssZ"))
}

/*case*/ class IssueListOption4Repository extends ToDo

case class IssueInput(
  title: Option[String] = None,
  body: Option[String] = None,
  assignee: Option[String] = None,
  milestone: Option[Int] = None,
  labels: Seq[String] = Nil,
  state: Option[IssueState] = None
) extends AbstractInput {
  override val value: JValue = {
    val a = assignee.map { s =>
      if (s.length == 0) JNull else JString(s)
    }.getOrElse(JNothing)
    val l = if (labels.length == 0) JNothing else JArray(labels.map(JString(_)).toList)

    ("title" -> title) ~
    ("body" -> body) ~
    ("assignee" -> a) ~
    ("milestone" -> milestone) ~
    ("labels" -> l) ~
    ("state" -> state.map(_.name))
  }
}

object IssueInput {
  def apply(title: String, body: Option[String], assignee: Option[String], milestone: Option[Int], labels: Seq[String]): IssueInput =
    IssueInput(Some(title), body, assignee, milestone, labels, None)
}
case class Issue(value: JValue) extends AbstractJson(value) {
  def number = get("number").toLong
  def title = get("title")
  def body = opt("body")

  lazy val assignee = objectOpt("assignee")(v => User(v))
  lazy val milestone = objectOpt("milestone")(v => Milestone(v))

  val state = get("state")
  lazy val user = new User(value \ "user")
  lazy val labels = (value \ "labels") match {
    case JArray(arr) => arr.map(new Label(_))
    case _ => Nil
  }
  lazy val repository = new Repository(value \ "repository")

  def comments = get("comments").toInt
  def created_at = getDate("created_at")
  def updated_at = getDate("updated_at")
  def closed_at = dateOpt("closed_at")
}
