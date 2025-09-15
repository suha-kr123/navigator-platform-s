package com.nivasafinance.features.lead.dto

data class LeadContacts(
    val contacts: List<Contact>
)

data class Contact(
    val name: String,
    val email: String,
    val phone: String
)
