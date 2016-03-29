package edu.ucdavis.fiehnlab.mona.backend.core.auth.jwt.types

import java.util

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import scala.annotation.meta.field

/**
  * secret to be used for the token
  * @param value
  */
case class TokenSecret(value:String)