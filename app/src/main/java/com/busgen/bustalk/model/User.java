package com.busgen.bustalk.model;

/**
 * Created by Johan on 2015-10-02.
 */
public class User implements IUser{

    private String userName;
    private String interest;

    public User(){}

    public User(String userName, String interest){
        this.userName = userName;
        this.interest = interest;
    }

    @Override
    public void setUserName(String userName) {
        System.out.println("Sätter namn till " + userName );
        this.userName = userName;
    }

    @Override
    public void setInterest(String interest) {
        this.interest = interest;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getInterest() {
        return interest;
    }

    @Override
    public boolean equals(Object object){
        if(object == this){
            return true;
        }
        if(object == null){
            return false;
        }
        if(object.getClass() != this.getClass()){
            return false;
        }

        if (this.getUserName().equals(((IUser)object).getUserName())){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hashCode = userName.hashCode();
        return hashCode;
    }

}
