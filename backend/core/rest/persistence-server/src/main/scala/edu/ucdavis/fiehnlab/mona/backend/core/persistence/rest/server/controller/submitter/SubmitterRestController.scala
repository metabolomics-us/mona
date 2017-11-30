package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import javax.servlet.{ServletRequest, ServletResponse}

import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.scheduling.annotation.AsyncResult
import org.springframework.web.bind.annotation._

/**
  * Created by wohlgemuth on 3/7/16.
  */
@CrossOrigin
@RestController
@RequestMapping(Array("/rest/submitters"))
class SubmitterRestController extends GenericRESTController[Submitter] {

  @Autowired
  val httpServletRequest: HttpServletRequest = null

  @Autowired
  val loginService: LoginService = null

  /**
    * Returns all the specified data in the system.
    *
    * @return
    */
  override def doList(page: Integer, size: Integer): Future[ResponseEntity[Iterable[Submitter]]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      super.doList(page, size)
    } else if (submitterMongoRepository.exists(loginInfo.username)) {
      new AsyncResult[ResponseEntity[Iterable[Submitter]]](
        new ResponseEntity[Iterable[Submitter]](Array(submitterMongoRepository.findOne(loginInfo.username)), HttpStatus.OK)
      )
    } else {
      new AsyncResult[ResponseEntity[Iterable[Submitter]]](new ResponseEntity[Iterable[Submitter]](Array.empty[Submitter], HttpStatus.OK))
    }
  }

  /**
    * Returns the specified submitter
    *
    * @param id
    * @return
    */
  override def doGet(id: String, servletRequest: ServletRequest, servletResponse: ServletResponse): Future[ResponseEntity[Submitter]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN") || id == loginInfo.username) {
      super.doGet(id, servletRequest, servletResponse)
    } else {
      new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.FORBIDDEN))
    }
  }

  /**
    * Saves a submitter or updates it.  This will depend on the utilized repository
    *
    * @param submitter
    * @return
    */
  override def doSave(submitter: Submitter): Future[ResponseEntity[Submitter]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      super.doSave(submitter)
    } else {
      val existingUser: Submitter = submitterMongoRepository.findOne(loginInfo.username)

      if (existingUser == null || existingUser.id == loginInfo.username) {
        super.doSave(submitter.copy(id = loginInfo.username))
      } else {
        new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.CONFLICT))
      }
    }
  }

  /**
    * Saves the provided submitter at the given path
    *
    * @param id
    * @param submitter
    * @return
    */
  override def doPut(id: String, submitter: Submitter): Future[ResponseEntity[Submitter]] = {

    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN") || id == loginInfo.username) {
      super.doPut(id, submitter)
    } else {
      new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.FORBIDDEN))
    }
  }

  /**
    * this is the utilized repository, doing all the heavy lifting
    */
  @Autowired
  val submitterMongoRepository: ISubmitterMongoRepository = null

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[Submitter, String] = submitterMongoRepository
}
