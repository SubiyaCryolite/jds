package io.github.subiyacryolite.jds.tests.constants

enum class Rights(
        private val displayText: String
) {
    CAN_LOGIN("This user can login"),
    CAN_CREATE_USER("This account can create and manage users"),
    CAN_DELETE_USER("This account can delete users"),
    CAN_CREATE_RECORD("This account can create system records"),
    CAN_DELETE_RECORD("This account can delete system records");

    override fun toString(): String {
        return displayText
    }
}