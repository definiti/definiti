package definiti.root.utils

sealed trait Validation[+A] {
  def isValid: Boolean

  def map[B](f: A => B): Validation[B]

  def flatMap[B](f: A => Validation[B]): Validation[B]

  def and[B](f: => Validation[B]): Validation[B]

  def filter[B](f: A => Validation[Nothing]): Validation[A]

  def foreach(f: A => Unit): Validation[A]

  def fold[B](onError: Seq[Error] => B, onValid: A => B): B
}

object Validation {
  def squash[A](validatedSet: Set[Validation[A]]): Validation[Seq[A]] = {
    squash(validatedSet.toSeq)
  }
  def squash[A](validatedSeq: Validation[A]*)(implicit dummyImplicit: DummyImplicit): Validation[Seq[A]] = {
    squash(validatedSeq)
  }
  def squash[A](validatedSeq: Seq[Validation[A]]): Validation[Seq[A]] = {
    if (validatedSeq.forall(_.isValid)) {
      Valid(validatedSeq.collect { case Valid(values) => values })
    } else {
      Invalid(validatedSeq.collect { case Invalid(errors) => errors }.flatten)
    }
  }

  def flatSquash[A](validatedSeq: Validation[Seq[A]]*)(implicit dummyImplicit: DummyImplicit): Validation[Seq[A]] = {
    flatSquash(validatedSeq)
  }
  def flatSquash[A](validatedSeq: Seq[Validation[Seq[A]]]): Validation[Seq[A]] = {
    squash(validatedSeq).map(_.flatten)
  }

  def both[A, B](validatedA: Validation[A], validatedB: Validation[B]): Validation[(A, B)] = {
    (validatedA, validatedB) match {
      case (Invalid(errorsA), Invalid(errorsB)) => Invalid(errorsA ++ errorsB)
      case (Invalid(errorsA), _) => Invalid(errorsA)
      case (_, Invalid(errorsB)) => Invalid(errorsB)
      case (Valid(valueA), Valid(valueB)) => Valid((valueA, valueB))
      case _ => throw new UnsupportedOperationException("Validated.both with Valid")
    }
  }

  def trying[A](op: => A): Validation[A] = {
    try {
      Valid(op)
    } catch {
      case exception: Exception => Invalid(exception.getMessage)
    }
  }

  def flatTrying[A](op: => Validation[A]): Validation[A] = {
    try {
      op
    } catch {
      case exception: Exception => Invalid(exception.getMessage)
    }
  }
}

case class Valid[+A](value: A) extends Validation[A] {
  override def isValid: Boolean = true

  override def map[B](f: (A) => B): Validation[B] = Valid(f(value))

  override def flatMap[B](f: (A) => Validation[B]): Validation[B] = f(value)

  override def and[B](f: => Validation[B]): Validation[B] = f

  override def filter[B](f: (A) => Validation[Nothing]): Validation[A] = {
    f(value) match {
      case Invalid(errors) => Invalid(errors)
      case _ => this
    }
  }

  override def foreach(f: (A) => Unit): Validation[A] = {
    f(value)
    this
  }

  override def fold[B](onError: (Seq[Error]) => B, onValid: (A) => B): B = onValid(value)
}

case class Invalid(errors: Seq[Error]) extends Validation[Nothing] {
  override def isValid: Boolean = false

  override def map[B](f: (Nothing) => B): Validation[B] = Invalid(errors)

  override def flatMap[B](f: (Nothing) => Validation[B]): Validation[B] = Invalid(errors)

  def join(other: Validation[Nothing]): Validation[Nothing] = other match {
    case Valid(_) => this
    case Invalid(otherErrors) => Invalid(errors ++ otherErrors)
  }

  override def and[B](f: => Validation[B]): Validation[B] = Invalid(errors)

  override def filter[B](f: (Nothing) => Validation[Nothing]): Validation[Nothing] = Invalid(errors)

  override def foreach(f: (Nothing) => Unit): Validation[Nothing] = this

  override def fold[B](onError: (Seq[Error]) => B, onValid: (Nothing) => B): B = onError(errors)
}

object Invalid {
  def apply(message: String): Invalid = new Invalid(Seq(Error(message)))
}

case class Error(messages: String)
