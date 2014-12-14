package com.tweetco.tweets;

public class UserProfile {
	public static final String CLASSNAME = "UserProfile";
	public static final String UserName = "UserName";
	public static final String DisplayName = "DisplayName";
	public static final String Image = "image";
	public static final String BImage = "BImage";
	public static final String Email = "Email";
	public static final String Followers = "Followers";
	public static final String Followees = "Followees";
	public static final String Company = "Company";
	public static final String Mobile = "Mobile";
	public static final String Title = "Title";
	

	private static Object lock;


	public UserProfile()
	{
		
	}

	public static void createUserWithData(String username, String displayName, String email,
			byte[] image, String title)
	{


	}

	public static UserProfile getUserProfile(String userName)
	{
		return null;
	}
	
	public static UserProfile getUserProfileForEmail(String email) 
	{
		return null;
	}
	
	public static UserProfile getUserProfileForEmail2(String email)
	{
		return null;
	}


//	public String getUserName()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(UserName);
//	}
//
//	public String getDisplayName()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(DisplayName);
//	}
//
//	public Bitmap getImage()
//	{
//		Bitmap bitmap = null;
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		byte[] imageBytes = null;
//		ParseFile imageObj = (ParseFile)user.get(Image);
//		if(imageObj != null)
//		{
//			try {
//				imageBytes = imageObj.getData();
//				bitmap = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				//b is the Bitmap
//
//				//calculate how many bytes our image consists of.
//				bitmap = BitmapFactory.decodeResource(WorxTweet.mContext.getResources(), R.drawable.ico_profile_default);
//				
//				
//				//Converting to bytes
//				
////				int bytes = bitmap.getByteCount();
////				//or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
////				//int bytes = b.getWidth()*b.getHeight()*4; 
////
////				ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
////				bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
////
////				imageBytes = buffer.array(); 
//			}
//		}
//		else
//		{
//			//b is the Bitmap
//
//			//calculate how many bytes our image consists of.
//			bitmap = BitmapFactory.decodeResource(WorxTweet.mContext.getResources(), R.drawable.ico_profile_default);
//			
//			
//			//Converting to bytes
//			
////			int bytes = bitmap.getByteCount();
////			//or we can calculate bytes this way. Use a different value than 4 if you don't use 32bit images.
////			//int bytes = b.getWidth()*b.getHeight()*4; 
////
////			ByteBuffer buffer = ByteBuffer.allocate(bytes); //Create a new buffer
////			bitmap.copyPixelsToBuffer(buffer); //Move the byte data to the buffer
////
////			imageBytes = buffer.array(); 
//		}
//		return bitmap;
//	}
//
//	public byte[] getBImage()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (byte[])user.get(BImage);
//	}
//
//	public String getEmail()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(Email);
//	}
//	
//	public String getCompany()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(Company);
//	}
//	
//	public String getTitle()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(Title);
//	}
//	
//	public String getMobile()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(Mobile);
//	}
//
//	public String getFollowers()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(Followers);
//	}
//
//	public void followUser(String username)
//	{
//		String followees = getFollowees();
//		if(TextUtils.isEmpty(followees))
//		{
//			followees = "";
//		}
//		followees = followees + ":"+ username;
//
//		user.put(Followees, followees);
//
//
//		try
//		{
//			UserProfile userProfile= getUserProfile(username);
//			if(userProfile!=null)
//			{
//				String followers = userProfile.getFollowers();
//				if(TextUtils.isEmpty(followers))
//				{
//					followers = "";
//				}
//				followers = followers + ":"+ username;
//
//				userProfile.user.put(Followers, followees);
//				userProfile.user.saveInBackground();
//			}
//		}
//		catch (ParseException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	public String getFollowees()
//	{
//		if(user == null)
//		{
//			throw new IllegalArgumentException();
//		}
//		return (String)user.get(Followees);
//	}	

}
