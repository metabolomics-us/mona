package edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.submitter

import java.util.Collections
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.domain.HelperTypes.LoginInfo
import edu.ucdavis.fiehnlab.mona.backend.core.domain.Submitter
import edu.ucdavis.fiehnlab.mona.backend.core.domain.service.LoginService
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.mongo.repository.ISubmitterMongoRepository
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.http.{ResponseEntity, HttpStatus}
import org.springframework.scheduling.annotation.{AsyncResult, Async}
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
    * saves a spectra or updates it. This will depend on the utilized repository
    *
    * @param submitter
    * @return
    */
  @Async
  @RequestMapping(path = Array(""), method = Array(RequestMethod.POST))
  @ResponseBody
  override def save(@RequestBody submitter: Submitter): AsyncResult[Submitter] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if(loginInfo.roles.contains("ADMIN")) {
      super.save(submitter)
    } else {
      super.save(submitter.copy(emailAddress = loginInfo.username))
    }
  }


  /**
    * saves the provided submitter at the given path
    *
    * @param id
    * @param submitter
    * @return
    */
  @Async
  @RequestMapping(path = Array("/{id}"), method = Array(RequestMethod.PUT))
  @ResponseBody
  override def put(@PathVariable("id") id: String, @Valid @RequestBody submitter: Submitter): Future[ResponseEntity[Submitter]] = {
    val token: String = httpServletRequest.getHeader("Authorization").split(" ").last
    val loginInfo: LoginInfo = loginService.info(token)

    if (loginInfo.roles.contains("ADMIN")) {
      super.put(id, submitter)
    } else {
      val existingUser: Submitter = submitterMongoRepository.findOne(id)

      if (loginInfo.username == existingUser.emailAddress) {
        super.put(id, submitter)
      } else {
        new AsyncResult[ResponseEntity[Submitter]](new ResponseEntity[Submitter](HttpStatus.FORBIDDEN))
      }
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
