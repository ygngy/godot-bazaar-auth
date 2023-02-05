/*
 * Copyright (c) 2023 Mohamadreza Amani
 * Github: https://github.com/ygngy/
 * Linkedin: https://www.linkedin.com/in/ygngy/
 * Email: amany1388@gmail.com
 */

package com.github.ygngy.godotbazaarauth

import android.app.Activity
import android.content.Intent
import android.view.View
import com.farsitel.bazaar.BazaarClientProxy
import com.farsitel.bazaar.BazaarResponse
import com.farsitel.bazaar.core.BazaarSignIn
import com.farsitel.bazaar.core.BazaarSignInClient
import com.farsitel.bazaar.core.model.BazaarSignInAccount
import com.farsitel.bazaar.core.model.BazaarSignInOptions
import com.farsitel.bazaar.core.model.SignInOption
import com.farsitel.bazaar.storage.BazaarStorage
import com.farsitel.bazaar.util.ext.toReadableString
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo

class MainAuth(godot: Godot): GodotPlugin(godot) {

    private companion object {
        private const val EMIT_GET_STORAGE = "get_storage" // with storage as String
        private const val EMIT_SET_STORAGE = "set_storage" // with storage as String
        private const val EMIT_USER_ID = "user_id" // with user id as String

        private const val REQ_CODE = 169

        private const val TAG = "GODOT BAZAAR AUTH"
        private fun log(msg: String, e: Exception? = null) {
            if (e == null) println("\n$TAG: $msg")
            else {
                println("\n$TAG: $msg - ERROR: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private lateinit var client: BazaarSignInClient


    // todo must be same as manifest metadata for godot
    override fun getPluginName() = "BazaarAuth"

    // todo register method names for godot
    override fun getPluginMethods() = mutableListOf(
        ::getUserId.name,
        ::saveData.name,
        ::getSavedData.name,
        ::hasBazaar.name,
        ::needAuthUpdate.name,
        ::needStorageUpdate.name,
        ::installBazaar.name,
        ::updateBazaar.name,
    )

    //todo register signal names for godot
    override fun getPluginSignals() = mutableSetOf(
        SignalInfo(EMIT_USER_ID, String::class.java),
        SignalInfo(EMIT_GET_STORAGE, String::class.java),
        SignalInfo(EMIT_SET_STORAGE, String::class.java),
    )

    override fun onMainCreate(activity: Activity?): View? {
        val view = super.onMainCreate(activity)
        val signInOption = BazaarSignInOptions.Builder(SignInOption.DEFAULT_SIGN_IN).build()
        client = BazaarSignIn.getClient(godot.requireContext(), signInOption)
        getUserId()
        needStorageUpdate()
        return view
    }

    override fun onMainDestroy() {
        BazaarStorage.disconnect(godot.requireContext())
        BazaarSignIn.disconnect(godot.requireContext())
        super.onMainDestroy()
    }

    override fun onMainActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onMainActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            checkLogIn(BazaarSignIn.getSignedInAccountFromIntent(data))
        }
    }

    /**
     * Helper method to check if user is logged in.
     * If user is logged in, notify godot with [EMIT_USER_ID],
     * otherwise retry to login.
     */
    private fun checkLogIn(account: BazaarSignInAccount?){
        if (hasBazaar()) {
            if (account != null) {
                try {
                    emitSignal(EMIT_USER_ID, account.accountId)
                }
                catch (e: Exception){
                    log("EMIT_USER_ID", e)
                }
            } else
                godot.startActivityForResult(client.getSignInIntent(), REQ_CODE)
        }
    }

    /**
     * Requests user id from Bazaar
     *
     * in case of success result will be emitted as [EMIT_USER_ID]
     *
     * returns true if process is started
     * otherwise shows bazaar installation or update dialog
     */
    fun getUserId(): Boolean = if (hasBazaar() && !needAuthUpdate()) {
        BazaarSignIn.getLastSignedInAccount(
            context = godot.requireContext(),
            owner = godot.requireActivity(),
            callback = { response ->
                checkLogIn(response?.data)
            })
        true
    } else false

    /**
     * Checks Bazaar installation
     *
     * if Bazaar is installed returns true,
     * otherwise returns false and shows install dialog
     */
    fun hasBazaar(): Boolean {
        val has = BazaarClientProxy.isBazaarInstalledOnDevice(godot.requireContext())
        if (!has) {
            installBazaar()
            log("<<BAZAAR IS NOT INSTALLED>>")
        }
        return has
    }

    /**
     * Checks Bazaar version for storage support
     *
     * if Bazaar needs update returns true and shows update dialog
     */
    fun needStorageUpdate(): Boolean {
        val need = BazaarClientProxy
            .isNeededToUpdateBazaar(godot.requireContext())
            .needToUpdateForStorage
        if (need) {
            updateBazaar()
            log("<<BAZAAR NEED UPDATE for Storage>>")
        }
        return need
    }

    /**
     * Checks Bazaar version for authentication support
     *
     * if Bazaar needs update returns true and shows update dialog
     */
    fun needAuthUpdate(): Boolean {
        val need = BazaarClientProxy
            .isNeededToUpdateBazaar(godot.requireContext())
            .needToUpdateForAuth
        if (need) {
            updateBazaar()
            log("<<BAZAAR NEED UPDATE for Auth>>")
        }
        return need
    }


    fun installBazaar(){
        BazaarClientProxy.showInstallBazaarView(godot.requireContext())
    }

    fun updateBazaar(){
        BazaarClientProxy.showUpdateBazaarView(godot.requireContext())
    }

    private fun extractResponse(response:  BazaarResponse<ByteArray?>?): String
        = response?.data?.toReadableString() ?: ""

    /**
     * Requests saved data from Bazaar
     *
     * in case of success result will be emitted as [EMIT_GET_STORAGE]
     *
     * returns true if process is started
     * otherwise shows bazaar installation or update dialog
     */
    fun getSavedData(): Boolean = if (hasBazaar() && !needStorageUpdate()) {
        BazaarStorage.getSavedData(
            context = godot.requireContext(),
            owner = godot.requireActivity(),
            callback = {
                try {
                    emitSignal(EMIT_GET_STORAGE, extractResponse(it))
                } catch (e: Exception) {
                    log("EMIT_GET_STORAGE", e)
                }
            }
        )
        true
    } else false

    /**
     * Requests to save [data] in Bazaar
     *
     * in case of success saved data will be emitted as [EMIT_SET_STORAGE]
     *
     * returns true if process is started
     * otherwise shows bazaar installation or update dialog
     */
    fun saveData(data: String): Boolean = if (hasBazaar() && !needStorageUpdate()) {
        BazaarStorage.saveData(
            context = godot.requireContext(),
            owner = godot.requireActivity(),
            data = data.toByteArray(),
            callback = {
                try {
                    emitSignal(EMIT_SET_STORAGE, extractResponse(it))
                } catch (e: Exception) {
                    log("EMIT_SET_STORAGE", e)
                }
            }
        )
        true
    } else false
}