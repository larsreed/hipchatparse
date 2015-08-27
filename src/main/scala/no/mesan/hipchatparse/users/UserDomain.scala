package no.mesan.hipchatparse.users

/** A user. */
case class User(ID: String, mention: Option[String]= None, fullName: String) {
  def withMention(name: String) = this.copy(mention=Some(name))
}

object NoUser extends User("", None, "")