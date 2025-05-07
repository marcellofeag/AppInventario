package com.example.aplicacion.sql

import androidx.compose.ui.text.input.TextFieldValue
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import android.util.Log
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.Date
import java.sql.PreparedStatement
import kotlin.math.abs

class ConexionSQL {
    fun validarUsuario(usuario: TextFieldValue, contra: TextFieldValue) : Array<String> {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var res: Array<String>
        var user : String = usuario.text
        var pass : String = contra.text
        if (user=="" || pass==""){
            return arrayOf("Debe rellenar todos los campos", user, "0")
        }
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet = statement.executeQuery("SELECT contrasena, nivel FROM usuario where nombre='$user'")
            if (resultSet.next()) {
                if (pass == resultSet.getString(1)){
                    res = arrayOf("Login correcto", user, resultSet.getString(2))
                }else{
                    res = arrayOf("Contraseña incorrecta", user, "0")
                }
            }else{
                res = arrayOf("Usuario no existe", user, "0")
            }
        } catch (e: ClassNotFoundException) {
            res = arrayOf("Fallo del Driver", "0")
        } catch (e: SQLException) {
            val mensajeError = e.cause.toString() ?: "Error desconocido"
            Log.e("TAG", "Error de SQL: $mensajeError")
            res = arrayOf(mensajeError, user, "0")
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                res = arrayOf("Fallo al cerrar la base de datos", user, "0")
            }
        }
        return res
    }

    fun buscarArticulo(codigo: String): Array<String> {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var res: Array<String> = emptyArray()
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT stock, nombre, proveedor, precio FROM articulo WHERE codigo='$codigo'")
            if (resultSet.next()) {
                val stock: String = resultSet.getString(1)
                val nombre: String = resultSet.getString(2)
                val proveedor: String = resultSet.getString(3)
                val precio: String = resultSet.getString(4)
                res = arrayOf(stock, nombre, proveedor, precio, codigo)
            }
        } catch (e: ClassNotFoundException) {
            Log.e("Error","Fallo del Driver: ${e.message}")
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            Log.e("Error","Error de SQL: $mensajeError")
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                Log.e("Error","Error al cerrar la base de datos: ${e.message}")
            }
        }
        return res
    }

    fun recepcionarArticulo(codigo: String): Array<String> {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var res: Array<String> = emptyArray()
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT nombre, cantidad_paquete FROM articulo WHERE codigo='$codigo'")
            if (resultSet.next()) {
                val nombre: String = resultSet.getString(1)
                val cantidadpaquete: String = resultSet.getString(2)
                res = arrayOf(nombre, cantidadpaquete)
            }
        } catch (e: ClassNotFoundException) {
            Log.e("Error","Fallo del Driver: ${e.message}")
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            Log.e("Error","Error de SQL: $mensajeError")
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                Log.e("Error","Error al cerrar la base de datos: ${e.message}")
            }
        }
        return res
    }

    fun anadirRecepcion(codigo: String, paquetes: Int, usuario: String): String {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var res = ""
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val filasAfectadas = statement.executeUpdate("UPDATE articulo SET stock = stock + cantidad_paquete * $paquetes WHERE codigo = '$codigo'")
            if (filasAfectadas > 0) {
                val resultSet: ResultSet = statement.executeQuery("SELECT precio, cantidad_paquete FROM articulo WHERE codigo='$codigo'")
                if (resultSet.next()) {
                    val precio: String = resultSet.getString(1)
                    val cantidadpaquete: String = resultSet.getString(2)
                    val valor: Float = paquetes * precio.toFloat() * cantidadpaquete.toInt()
                    val fechahora = obtenerFechaHoraActual()
                    val sql1 = "INSERT INTO recepcion VALUES (?, ?, ?, ?, ?)"
                    preparedStatement = connection.prepareStatement(sql1)
                    preparedStatement.setString(1, usuario)
                    preparedStatement.setString(2, codigo)
                    preparedStatement.setInt(3, paquetes)
                    preparedStatement.setInt(4, cantidadpaquete.toInt()*paquetes)
                    preparedStatement.setString(5, fechahora)
                    preparedStatement.executeUpdate()
                    val sql2 = "INSERT INTO actividad VALUES (?, ?, ?, ?, ?)"
                    preparedStatement = connection.prepareStatement(sql2)
                    preparedStatement.setString(1, usuario)
                    preparedStatement.setString(2, "recepcion")
                    preparedStatement.setString(3, codigo)
                    preparedStatement.setFloat(4, valor)
                    preparedStatement.setString(5, fechahora)
                    preparedStatement.executeUpdate()

                }
                return "Correcto"
            } else {
                return "Incorrecto"
            }
        } catch (e: ClassNotFoundException) {
           res = "Fallo del Driver: ${e.message}"
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            res = "Error de SQL: $mensajeError"
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                res = "Error al cerrar la base de datos: ${e.message}"
            }
        }
        return res
    }

    fun anadirRegularizacion(codigo: String, cantidadNueva: Int, precio: Double, usuario: String): String {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var preparedStatement: PreparedStatement? = null
        var res = ""
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement1: Statement = connection.createStatement()
            val statement2: Statement = connection.createStatement()
            val resultSet: ResultSet = statement1.executeQuery("SELECT stock FROM articulo WHERE codigo='$codigo'")
            resultSet.next()
            val stock: Int = resultSet.getString(1).toInt()
            val filasAfectadas = statement2.executeUpdate("UPDATE articulo SET stock = $cantidadNueva, precio = $precio WHERE codigo = '$codigo'")
            if (filasAfectadas > 0) {
                    var diferencia = abs(stock-cantidadNueva)
                    var valor: Double = diferencia * precio
                    var fechahora = obtenerFechaHoraActual()
                    val sql = "INSERT INTO actividad VALUES (?, ?, ?, ?, ?)"
                    preparedStatement = connection.prepareStatement(sql)
                    preparedStatement.setString(1, usuario)
                    preparedStatement.setString(2, "regularizacion")
                    preparedStatement.setString(3, codigo)
                    preparedStatement.setDouble(4, valor)
                    preparedStatement.setString(5, fechahora)
                    preparedStatement.executeUpdate()
                return "Correcto"
            } else {
                return "Incorrecto"
            }
        } catch (e: ClassNotFoundException) {
            res = "Fallo del Driver: ${e.message}"
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
           res = "Error de SQL: $mensajeError"
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                res = "Error al cerrar la base de datos: ${e.message}"
            }
        }
        return res
    }

    fun obtenerFechaHoraActual(): String {
        // Crear un objeto Date con la fecha y hora actual
        val fechaHoraActual = Date()

        // Crear un formateador de fecha y hora
        val formateador = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        // Formatear la fecha y hora actual
        val fechaHoraFormateada = formateador.format(fechaHoraActual)

        return fechaHoraFormateada
    }

    fun obtenerActividades(): MutableList<Array<String>> {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var matriz: MutableList<Array<String>> = mutableListOf()

        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT * from actividad")
            while(resultSet.next()){
                val usuario = resultSet.getString("usuario")
                val accion = resultSet.getString("accion")
                val articulo = resultSet.getString("articulo")
                val valor = resultSet.getString("valor")
                val fecha_hora = resultSet.getString("fecha_hora")
                matriz.add(arrayOf(usuario, accion, articulo, valor, fecha_hora))
            }
            return matriz
        } catch (e: ClassNotFoundException) {
            Log.e("Error","Fallo del Driver: ${e.message}")
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            Log.e("Error","Error de SQL: $mensajeError")
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                Log.e("Error","Error al cerrar la base de datos: ${e.message}")
            }
        }
        return matriz
    }

    fun buscarUsuario(usuario: String): Array<String> {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var res: Array<String> = emptyArray()
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT contrasena, nivel FROM usuario WHERE nombre='$usuario'")
            if (resultSet.next()) {
                val contra: String = resultSet.getString(1)
                val nivel: String = resultSet.getString(2)
                res = arrayOf(contra, nivel)
            }
        } catch (e: ClassNotFoundException) {
            Log.e("Error","Fallo del Driver: ${e.message}")
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            Log.e("Error","Error de SQL: $mensajeError")
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                Log.e("Error","Error al cerrar la base de datos: ${e.message}")
            }
        }
        return res
    }
    fun guardarUsuario(usuario: String, contra:String, nivel:String): String {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var res: String
        var preparedStatement: PreparedStatement?
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT 1 FROM usuario WHERE nombre='$usuario'")
            if (resultSet.next()) {
                statement.executeUpdate("UPDATE usuario SET contrasena = '$contra', nivel = '$nivel' WHERE nombre = '$usuario'")
                res = "Usuario $usuario actualizado"
            } else {
                val sql = "INSERT INTO usuario VALUES (?, ?, ?)"
                preparedStatement = connection.prepareStatement(sql)
                preparedStatement.setString(1,usuario)
                preparedStatement.setString(2,contra)
                preparedStatement.setString(3, nivel)
                preparedStatement.executeUpdate()
                res = "Usuario $usuario creado"
            }
        } catch (e: ClassNotFoundException) {
            Log.e("ClassNotFound","Fallo del Driver: ${e.message}")
            res = "Fallo del Driver: ${e.message}"
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            Log.e("SQL", "Error de SQL: $mensajeError")
            res = "Error de SQL: $mensajeError"
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                Log.e("SQL","Error al cerrar la base de datos: ${e.message}")
            }
        }
        return res
    }

    fun borrarUsuario(usuario: String): String {
        val url = "jdbc:mysql://BASE-mysql.services.clever-cloud.com:3306/BASE?useSSL=false"
        val username = "USUARIO"
        val password = "CONTRASENA"
        var connection: Connection? = null
        var res: String
        try {
            Class.forName("com.mysql.jdbc.Driver")
            connection = DriverManager.getConnection(url, username, password)
            val statement: Statement = connection.createStatement()
            val resultSet: ResultSet = statement.executeQuery("SELECT 1 FROM usuario WHERE nombre='$usuario'")
            if (resultSet.next()) {
                statement.executeUpdate("DELETE FROM usuario WHERE nombre = '$usuario'")
                res = "Usuario $usuario borrado"
            } else {
                res = "Usuario no encontrado"
            }
        } catch (e: ClassNotFoundException) {
            Log.e("ClassNotFound","Fallo del Driver: ${e.message}")
            res = "Fallo del Driver: ${e.message}"
        } catch (e: SQLException) {
            val mensajeError = e.message ?: "Error desconocido"
            Log.e("SQL", "Error de SQL: $mensajeError")
            res = "Error de SQL: $mensajeError"
        } finally {
            try {
                connection?.close()
            } catch (e: SQLException) {
                Log.e("SQL","Error al cerrar la base de datos: ${e.message}")
            }
        }
        return res
    }
}
