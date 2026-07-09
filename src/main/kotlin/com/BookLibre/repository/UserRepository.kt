package com.BookLibre.repository

import com.BookLibre.domain.User
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : CrudRepository<User, Int> {


    fun findByProps_Email(email: String): User?

    fun findTop5ByOrderByPropsBiblioKarmasDesc() : List<User>

}