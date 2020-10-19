package com.surpriseme.user.fragments.changepasswordfragment

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.google.android.material.textview.MaterialTextView
import com.surpriseme.user.R
import com.surpriseme.user.databinding.FragmentChangePasswordBinding
import com.surpriseme.user.activity.mainactivity.MainActivity
import com.surpriseme.user.fragments.viewprofile.ProfileFragment
import com.surpriseme.user.retrofit.RetrofitClient
import com.surpriseme.user.util.Constants
import com.surpriseme.user.util.PrefrenceShared
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordFragment : Fragment(), View.OnClickListener {

    private lateinit var binding:FragmentChangePasswordBinding
    private var oldPassword=""
    private var newPassword=""
    private var confirmPassword=""
    private var oldPassValue = ""
    private lateinit var shared:PrefrenceShared
    private lateinit var ctx:Context
    private lateinit var tbackpress:ImageView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ctx = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_change_password,container,false)
        val view = binding.root
        shared = PrefrenceShared(ctx)
        tbackpress = view.findViewById(R.id.tbackpress)

        init()

        return view
    }

    private fun init() {

        binding.continueButton.setOnClickListener(this)
        tbackpress.setOnClickListener(this)
        oldPassValue = shared.getString(Constants.DataKey.OLD_PASS_VALUE)

    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.continueButton -> {

                oldPassword = binding.oldPassEdt.text.toString().trim()
                newPassword = binding.newPasswordEdt.text.toString().trim()
                confirmPassword = binding.confirmPassEdt.text.toString().trim()

                when {
                    oldPassword.isEmpty() -> {
                        binding.oldPassEdt.error = getString(R.string.please_fill_require_field)
                    }
                    newPassword.isEmpty() -> {
                        binding.newPasswordEdt.error = getString(R.string.please_fill_require_field)
                    }
                    confirmPassword.isEmpty() -> {
                        binding.confirmPassEdt.error = getString(R.string.please_fill_require_field)
                    }
                    newPassword != confirmPassword -> {
                        binding.newPasswordEdt.error = getString(R.string.password_not_match)
                    }
                    oldPassword != oldPassValue -> {
                        binding.oldPassEdt.error = getString(R.string.please_use_valid_old_password)
                    }
                    else -> {
                        // hit Change password api....
                        changePasswordApi()
                    }
                }
            }
            R.id.tbackpress -> {
                replaceFragment(ProfileFragment())
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {

        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(R.id.frameContainer, fragment)
        transaction?.addToBackStack("fragment")
        transaction?.commit()
    }

    private fun changePasswordApi() {

        binding.loaderLayout.visibility = View.VISIBLE
        RetrofitClient.api.changePasswordApi(shared.getString(Constants.DataKey.AUTH_VALUE),oldPassword,newPassword,confirmPassword)
            .enqueue(object :Callback<ChangePasswordModel> {
                override fun onResponse(
                    call: Call<ChangePasswordModel>,
                    response: Response<ChangePasswordModel>
                ) {
                    binding.loaderLayout.visibility = View.GONE
                    if (response.body() !=null) {
                        if (response.isSuccessful) {
                            shared.setString(Constants.DataKey.OLD_PASS_VALUE, newPassword)
                            alertPopUp(response.body()?.data?.message!!)
                        }
                    } else {
                        val jsonObject:JSONObject
                        if (response.errorBody() !=null) {
                            try {
                                jsonObject = JSONObject(response.errorBody()?.string()!!)
                                val errorMessage = jsonObject.getString(Constants.ERRORS)
//                                alertPopUp(errorMessage)
                                Toast.makeText(ctx,"" + errorMessage,Toast.LENGTH_SHORT).show()
                            }catch (e:JSONException) {
                                Toast.makeText(ctx,"" + e.message.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<ChangePasswordModel>, t: Throwable) {
                    binding.loaderLayout.visibility = View.GONE
                    Toast.makeText(ctx,"" + t.message.toString(),Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun alertPopUp(message: String) {

        val layoutInflater: LayoutInflater =
            ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val popUp: View = layoutInflater.inflate(R.layout.booking_done_popup, null)
        val popUpWindowReport = PopupWindow(
            popUp, ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT, true
        )
        popUpWindowReport.showAtLocation(popUp, Gravity.CENTER, 0, 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            popUpWindowReport.elevation = 10f
        }
        popUpWindowReport.isTouchable = false
        popUpWindowReport.isOutsideTouchable = false

        val messageText: MaterialTextView = popUp.findViewById(R.id.messageDispText)
        messageText.text = message
        val done: MaterialTextView = popUp.findViewById(R.id.doneTv)

        done.setOnClickListener {
            popUpWindowReport.dismiss()
            val intent = Intent(ctx, MainActivity::class.java)
            startActivity(intent)
        }
    }

}