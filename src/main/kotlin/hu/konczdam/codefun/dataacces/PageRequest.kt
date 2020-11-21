package hu.konczdam.codefun.dataacces

class PageRequest (
        val page: Int,
        val size: Int,
        val sortProperty: String,
        val sortDirection: String = "asc"
)