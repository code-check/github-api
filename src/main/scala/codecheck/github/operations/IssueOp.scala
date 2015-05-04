package codecheck.github.operations

import java.net.URLEncoder
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import org.json4s.JArray
import org.json4s.JString
import org.json4s.JNothing

import codecheck.github.api.GitHubAPI
import codecheck.github.models.IssueInput
import codecheck.github.models.Issue
import codecheck.github.models.IssueListOption
import codecheck.github.models.IssueListOption4Repo

import codecheck.github.utils.ToDo

trait IssueOp {
  self: GitHubAPI =>

  private def doList(path: String): Future[List[Issue]] = {
    exec("GET", path).map( 
      _.body match {
        case JArray(arr) => arr.map(v => Issue(v))
        case _ => throw new IllegalStateException()
      }
    )
  }

  def listAllIssues(option: IssueListOption = IssueListOption()): Future[List[Issue]] = 
    doList("/issues" + option.q)

  def listUserIssues(option: IssueListOption = IssueListOption()): Future[List[Issue]] = 
    doList("/user/issues" + option.q)

  def listOrgIssues(org: String, option: IssueListOption = IssueListOption()): Future[List[Issue]] =
    doList(s"/orgs/$org/issues" + option.q)

  def listRepositoryIssues(owner: String, repo: String, option: IssueListOption4Repo): Future[List[Issue]] = ToDo[Future[List[Issue]]]
  def getIssue(owner: String, repo: String, number: Long): Future[Issue] = ToDo[Future[Issue]]
  def createIssue(owner: String, repo: String, input: IssueInput): Future[Issue] = ToDo[Future[Issue]]

  def editIssue(owner: String, repo: String, number: Long, input: IssueInput): Future[Issue] = {
    val path = s"/repos/$owner/$repo/issues/$number"
    val body = input.value
    exec("PATCH", path, body).map { result =>
      new Issue(result.body)
    }
  }

  def assign(owner: String, repo: String, number: Long, assignee: String): Future[Issue] = {
    editIssue(owner, repo, number, IssueInput(assignee=Some(assignee)))
  }

  def unassign(owner: String, repo: String, number: Long): Future[Issue] = {
    editIssue(owner, repo, number, IssueInput(assignee=Some("")))
  }

}
