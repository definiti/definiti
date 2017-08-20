package definiti.root.utils

object Dummies {
  class DummyImplicit2

  object DummyImplicit2 {
    implicit def dummyImplicit2: DummyImplicit2 = new DummyImplicit2
  }

  class DummyImplicit3

  object DummyImplicit3 {
    implicit def dummyImplicit3: DummyImplicit3 = new DummyImplicit3
  }
}
