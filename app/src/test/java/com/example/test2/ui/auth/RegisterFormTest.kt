package com.example.test2.ui.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegisterFormTest {

    private val valid = RegisterForm(
        fullName = "Budi Santoso",
        email = "budi@qudu.test",
        password = "secret1",
        phoneNumber = "081200000000",
        nik = "3171234567890001",
        address = "Jl. Merdeka 1",
        birthPlace = "Jakarta",
        birthDate = "1995-04-12",
        occupation = "Engineer",
    )

    @Test
    fun `a complete form has no error`() {
        assertNull(valid.firstError())
    }

    @Test
    fun `each missing field is named`() {
        mapOf(
            valid.copy(fullName = " ") to "Full name is required.",
            valid.copy(email = "") to "Email is required.",
            valid.copy(phoneNumber = "") to "Phone number is required.",
            valid.copy(address = "") to "Address is required.",
            valid.copy(birthPlace = "") to "Place of birth is required.",
            valid.copy(occupation = "") to "Occupation is required.",
            valid.copy(citizenship = "") to "Please select your citizenship.",
        ).forEach { (form, message) -> assertEquals(message, form.firstError()) }
    }

    @Test
    fun `an email without an at sign is rejected`() {
        assertEquals("That email address is not valid.", valid.copy(email = "budi.qudu.test").firstError())
    }

    @Test
    fun `a password shorter than six characters is rejected`() {
        assertEquals("The password must be at least 6 characters.", valid.copy(password = "12345").firstError())
    }

    @Test
    fun `six characters is enough`() {
        assertNull(valid.copy(password = "123456").firstError())
    }

    @Test
    fun `the NIK must be exactly sixteen characters`() {
        assertEquals("The NIK must be 16 digits.", valid.copy(nik = "317123456789000").firstError())
        assertEquals("The NIK must be 16 digits.", valid.copy(nik = "31712345678900011").firstError())
    }

    @Test
    fun `the NIK length check does not look at whether they are digits`() {
        assertNull(valid.copy(nik = "ABCDEFGHIJKLMNOP").firstError())
    }

    @Test
    fun `the birth date must be picked in year-month-day form`() {
        listOf("", "12-04-1995", "1995/04/12", "1995-4-12").forEach {
            assertEquals(it, "Please select your date of birth.", valid.copy(birthDate = it).firstError())
        }
    }

    @Test
    fun `the birth date format is checked, not whether the day exists`() {
        assertNull(valid.copy(birthDate = "1995-13-45").firstError())
    }

    @Test
    fun `errors are reported one at a time, top of the form first`() {
        assertEquals("Full name is required.", RegisterForm().firstError())
    }

    @Test
    fun `the defaults are male and Indonesian`() {
        assertEquals("MALE", RegisterForm().sex)
        assertEquals("WNI", RegisterForm().citizenship)
    }

    @Test
    fun `the request body trims every free text field`() {
        val dto = valid.copy(
            fullName = "  Budi  ",
            email = " budi@qudu.test ",
            phoneNumber = " 0812 ",
            nik = " 3171234567890001 ",
            address = " Jl. A ",
            birthPlace = " Jakarta ",
            birthDate = " 1995-04-12 ",
            occupation = " Engineer ",
        ).toDto("123456")

        assertEquals("Budi", dto.fullName)
        assertEquals("budi@qudu.test", dto.email)
        assertEquals("0812", dto.phoneNumber)
        assertEquals("3171234567890001", dto.nik)
        assertEquals("Jl. A", dto.address)
        assertEquals("Jakarta", dto.birthPlace)
        assertEquals("1995-04-12", dto.birthDate)
        assertEquals("Engineer", dto.occupation)
    }

    @Test
    fun `the request body keeps the password exactly as typed`() {
        assertEquals(" pass word ", valid.copy(password = " pass word ").toDto("1").password)
    }

    @Test
    fun `the request body registers a customer account with the code`() {
        val dto = valid.toDto("654321")

        assertEquals("CUSTOMER", dto.accountType)
        assertEquals("654321", dto.otp)
    }

    @Test
    fun `the code and password lengths match what the screens enforce`() {
        assertEquals(6, RESET_CODE_LENGTH)
        assertEquals(6, REGISTRATION_OTP_LENGTH)
        assertEquals(8, MIN_RESET_PASSWORD_LENGTH)
    }
}
