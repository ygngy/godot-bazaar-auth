# Godot game engine Android plugin for Bazaar authentication and in-app storage

<br>

### [مشاهده این صفحه به زبان فارسی](README.md)


<br>

With the help of this plugin, you can add [CafeBazaar](https://cafebazaar.ir) authentication and in-app storage to games made with the [Godot](https://godotengine.org) game engine. For this, just download the zip file in the [Release](https://github.com/ygngy/godot-bazaar-auth/releases) section and copy its content to the **android/plugins** folder. You can read how to use the Android plugin in Godot [here](https://docs.godotengine.org/en/stable/tutorials/plugins/android/android_plugin.html#loading-and-using-an-android-plugin).

 
**Note: You must be connected to the Internet when exporting the Android APK.**
<br>

## How to use this plugin in Godot script

Here is a sample godot script for using this plugin. You may use it in another way as your needs.

```python

var bazaar_auth = "BazaarAuth" # <<--- plugin name
var auth = null

func _ready():
	if Engine.has_singleton(bazaar_auth):
		auth = Engine.get_singleton(bazaar_auth)
		auth.connect("get_storage", self, "_on_get_storage") # <--- save receiver event
		auth.connect("set_storage", self, "_on_set_storage") # <--- save confirmation event
		auth.connect("user_id", self, "_on_got_user_id") # <--- user_id receiver event
		
		auth.getUserId() # <--- requesting user_id from the Bazaar
	else:
		print("========= Error! Auth plugin NOT FOUND ===========")


# The function of receiving game saves from the Bazaar
func _on_get_storage(data: String):
	print("get stored data: " + data)
	

# Function to confirm that the game is saved in the Bazaar
func _on_set_storage(data: String):
	print("set stored data: " + data)
	

# The function of receiving the user_id from the Bazaar
func _on_got_user_id(id: String):
	print("Bazaar Auth user id: " + id)
	# After authentication, you can get user data from the Bazaar or set user data in the Bazaar
	auth.saveData("sample user score is 123") # optionally storing a sample data in the Bazaar
	auth.getSavedData() # optionally requesting game saves from the Bazaar







#========================== Events of the Plugin ========================================
# This plugin only has three events: "get_storage" , "set_storage" , "user_id"
# All three events carry one "String" data type
# The "user_id" carries user's id 
# "set_storage" and "get_storage" both carry saved data


#========================== Main Functions of the Plugin ======================================
# With these three main functions ("getUserId", "saveData", "getSavedData"), you can use all features of this plugin.

#>>>>  getUserId(): boolean   <<<< requesting for user's id
# This function is used to request the user ID from the Bazaar
# If the Bazaar is not installed or not updated, the output is "false" and the Bazaar installation dialog will be displayed to the user
# otherwise the output of the function is "true", and the ID will be emitted through the "user_id" event.

#>>>>   saveData(String): boolean  <<<< saving the game in Bazaar
# This function is used to store game data in the Bazaar
# If the Bazaar is not installed or not updated, the output is "false" and the Bazaar installation dialog will be displayed to the user
# otherwise the output of the function is "true", and once the data is saved the game data will be emitted through the "set_storage" event.

#>>>>   getSavedData(): boolean    <<<< requesting for saved data
# This function is used to get game data from the Bazaar
# If the Bazaar is not installed or not updated, the output is "false" and the Bazaar installation dialog will be displayed to the user
# otherwise the output of the function is "true", and game data will be emitted through the "get_storage" event.



#============================ Helper Functions of the Plugin ==========================================
# When using the above functions, the following functions will be used automatically
# So you don't need to call the following functions yourself, but you can use them if needed

#   hasBazaar(): boolean    <========== Check whether the Bazaar is installed
# If the Bazaar is installed, the output is "true"
# But if the Bazaar is not installed, the output is "false" and the Bazaar installation dialog will be displayed to the user

#   needAuthUpdate(): boolean     <====== check the need to update the Bazaar for authentication
# the output is "false" only if Bazaar do not need to update for authentication
# But if the Bazaar is not updated, the output is "true" and the Bazaar update dialog will be displayed to the user

#   needStorageUpdate(): boolean  <====== check the need to update the Bazaar for storage
# the output is "false" only if Bazaar do not need to update for storage
# But if the Bazaar is not updated, the output is "true" and the Bazaar update dialog will be displayed to the user

#   installBazaar()   <======== requesting for Bazaar installation 
#   updateBazaar()    <======== requesting for Bazaar update 


```


---------------------------------------------------------------------------


**"Mohamadreza Amani"**  

My LinkedIn Profile: [https://www.linkedin.com/in/ygngy](https://www.linkedin.com/in/ygngy)

My Github Profile: [https://github.com/ygngy](https://github.com/ygngy)  

Email:  [amany1388@gmail.com](mailto:amany1388@gmail.com)