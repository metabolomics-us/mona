package edu.ucdavis.fiehnlab.mona.backend.core.auth.rest.controller

import edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.repository.UserRepository
import edu.ucdavis.fiehnlab.mona.backend.core.auth.types.User
import edu.ucdavis.fiehnlab.mona.backend.core.persistence.rest.server.controller.GenericRESTController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.web.bind.annotation.{RequestMapping, RestController}

/**
  * Created by wohlgemuth on 4/4/16.
  */
@RestController
@RequestMapping(value = Array("/rest/users"))
class UserController extends GenericRESTController[User]{

  @Autowired
  val userRepository: UserRepository = null

  /**
    * utilized repository
    *
    * @return
    */
  override def getRepository: PagingAndSortingRepository[User, String] = userRepository
}
