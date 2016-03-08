import java.security.Permission

class TestSecurityManager extends SecurityManager
{
  private val enabled = new InheritableThreadLocal[Boolean]

  override def checkPermission(perm: Permission) {
    if (enabled.get) {
      throw new SecurityException("Not permitted")
    }
  }

  def secured[R](f: => R) = {
    enabled.set(true)
    try {
      f
    } finally {
      enabled.set(false)
    }
  }
}
