package edu.ucdavis.fiehnlab.mona.backend.core.auth.types

import java.util


/**
  * defines the access role for a given user
  *
  * @param name
  */
case class Role(name: String)

/**
  * Defines a user account in the system
  *
  * @param username
  * @param password
  * @param roles
  */
case class User(
                 username: String,
                 password: String,
                 roles: java.util.List[Role] = new util.ArrayList[Role]()
               )
