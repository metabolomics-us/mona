package edu.ucdavis.fiehnlab.mona.backend.core.auth.types

import java.util

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field


/**
  * defines the access role for a given user
  *
  * @param name
  */
case class Role(name: String)

/**
  * defines an account in the system
  *
  * @param username
  * @param password
  * @param roles
  */
@Document(collection = "USER_AUTH")
case class User(
                 @(Id@field)
                 username: String,
                 password: String,
                 roles: java.util.List[Role] = new util.ArrayList[Role]()
               )


/**
  * secret to be used for the token
  * @param value
  */
case class TokenSecret(value:String)