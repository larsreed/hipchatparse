package no.mesan.hipchatparse.users

case class User(ID: String, mention: Option[String]= None, fullName: String) {
  def withMention(name: String) = this.copy(mention=Some(name))
}

object NoUser extends User("", None, "")