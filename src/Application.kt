package com.powrolx.ktmaghazekala

import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import kotlinx.html.*
import org.jetbrains.exposed.dao.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.*
import java.sql.Connection
import javax.management.Query


fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

object Users : IntIdTable() {
    val nickname = varchar("nickname", 50)
    val email = varchar("email", 50)
    val phonNumber = integer("phonNumber").nullable()
    val password = integer("pass").nullable()
}

data class UsersData(
    var id: Int,
    var nickname: String,
    var email: String,
    var phoneNumber: Int,
    var password: Int
) {
    companion object {
        fun from(source: Query): List<UsersData> = source.map {
            UsersData(
                id = it[Users.id].value,
                nickname = it[Users.nickname],
                email = it[Users.email],
                phoneNumber = it[Users.phonNumber]!!,
                password = it[Users.password]!!
            )
        }
    }
}


fun initDB() {
    Database.connect("jdbc:sqlite:website/data.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
    transaction {
        SchemaUtils.create(Users)
    }
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {

    initDB()

    install(ContentNegotiation) {
        gson {

        }
    }
    routing {
        get("/maghazekala/users/read") {
            lateinit var result: List<UsersData>
            transaction {
                result = UsersData.from(Users.selectAll())
            }
            call.respond(result)
        }

        get("/maghazekala/users/read/{id}") {


            var getmyId: String? = call.parameters["id"]
            var id: Int = getmyId!!.toInt()
            var result: List<UsersData>? = null
            transaction {
                result = UsersData.from(Users.select { Users.id eq id })
            }
            call.respond(result!!)
        }
        get("/maghazekala/users/insert/{nickname}/{email}/{phonNumber}/{password}") {
            var id: EntityID<Int>? = null
            var result: List<UsersData>? = null
            //var hamid = call.parameters.getAll("param")


            transaction {
                id = Users.insertAndGetId {
                    it[nickname] = call.parameters["nickname"]!!
                    it[email] = call.parameters["email"]!!
                    it[phonNumber] = call.parameters["phonNumber"]!!.toInt()
                    it[password] = call.parameters["password"]!!.toInt()
                }
                result = UsersData.from(Users.select { Users.id eq id!!})
            }
            call.respond(result!!)
        }

        get("/maghazekala/users/update/{id}/{nickname}/{email}/{phonNumber}/{password}") {
            var getmyId: String? = call.parameters["id"]
            var id: Int = getmyId!!.toInt()
            lateinit var result: List<UsersData>
            transaction {
                Users.update ({Users.id eq id}){
                    it[nickname] = call.parameters["nickname"]!!
                    it[email] = call.parameters["email"]!!
                    it[phonNumber] = call.parameters["phonNumber"]!!.toInt()
                    it[password] = call.parameters["password"]!!.toInt()
                }
                result = UsersData.from(Users.select { Users.id eq id})
            }
            call.respond(result!!)
        }

        get("/maghazekala/users/remove/{id}") {
            var id: Int = call.parameters["id"]!!.toInt()
            transaction {
                Users.deleteWhere {Users.id eq id}
            }
        }
    }

    transaction {
        val result = Users.selectAll().toMutableList()
        for (record in result) {
            println(
                record
                //record[Users.id],
                //record[Users.nickname],
                //record[Users.phonNumber],
                //record[Users.password]
            )

        }
    }

}