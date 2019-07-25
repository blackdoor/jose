package black.door.jose

import org.scalatest.matchers.{BePropertyMatchResult, BePropertyMatcher}

package object test {

  object left extends BePropertyMatcher[Either[Any, Any]] {

    def apply(either: Either[Any, Any]): BePropertyMatchResult =
      BePropertyMatchResult(either.isLeft, "left")
  }

  object right extends BePropertyMatcher[Either[Any, Any]] {

    def apply(either: Either[Any, Any]): BePropertyMatchResult =
      BePropertyMatchResult(either.isRight, "right")
  }
}
