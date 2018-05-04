'use strict';

export default class CookieService {

  constructor($cookies) {}

  stringToBoolean(s) {
    switch(s) {
      case "true":
      case "yes":
      case "1":
        return true;
      case "false":
      case "no":
      case "0":
      case null:
        return false;
      default:
        return Boolean(string);
    }
  }

  get(name) {
    return $cookies.get(name);
  }

  update(name, value) {
    return $cookies.put(name, value);
  }

  remove(name) {
    return $cookies.remove(name);
  }

  /**
   * Return a boolean form of the value stored in a cookie
   */
  getBooleanValue(name, defaultValue = false) {
    if ($cookieStore.get(cookieName)) {
      return stringToBoolean($cookieStore.get(name));
    } else {
      return defaultValue;
    }
  }
}

CookieService.$inject = ['$cookies'];
