package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most once group
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<String>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception {
        //If the mobile number exists in database, throw "User already exists" exception
        //Otherwise, create the user and return "SUCCESS"
        //your code here

        if (userMobile.contains(mobile)) {
            throw new Exception("User already exists");
        } else {
            userMobile.add(mobile);
            return "SUCCESS";
        }

    }

    public Group createGroup(List<User> users) {

        if (users.size() < 2) {
            throw new IllegalArgumentException("A group must have at least two users");
        }

        Group group = null;
        if (users.size() == 2)
        {
            group = new Group(users.get(1).getName(), users.size());
        }
        if (users.size() > 2)
        {
            group = new Group("Group " + (++customGroupCount), users.size());
        }

        groupUserMap.put(group, users);
        groupMessageMap.put(group, new ArrayList<>());

        adminMap.put(group, users.get(0));

        return group;
    }


    public int createMessage(String content){
        // The 'i^th' created message has message id 'i'.
        // Return the message id.
        //your code here
        Message message = new Message((++messageId), content, new Date());

        return messageId;
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "You are not allowed to send message" if the sender is not a member of the group
        //If the message is sent successfully, return the final number of messages in that group.
        //your code here

        if (!groupUserMap.containsKey(group))
        {
            throw new RuntimeException("Group does not exist");
        }

        List<User> groupUsers = new ArrayList<>();
        groupUsers = groupUserMap.get(group);

        if (!groupUsers.contains(sender))
        {
            throw new RuntimeException("You are not allowed to send message");
        }

        groupMessageMap.get(group).add(message);
        senderMap.put(message, sender);
        return groupMessageMap.get(group).size();


    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group
        //Change the admin of the group to "user" and return "SUCCESS".

        //your code here
        if (!groupUserMap.containsKey(group))
        {
            throw new RuntimeException("Group does not exist");
        }

        if (adminMap.get(group) != approver)
        {
            throw new RuntimeException("Approver does not have rights");
        }

        if (!groupUserMap.get(group).contains(user))
        {
            throw new RuntimeException("User is not a participant");
        }

        adminMap.put(group, user);

        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.
        //If user is removed successfully, return (the updated number of users in the group + the updated number of messages in group + the updated number of overall messages)
        //your code here
        Group groupToRemove = null;
        for (Map.Entry<Group, List<User>> entry : groupUserMap.entrySet())
        {
            if (entry.getValue().contains(user))
            {
                groupToRemove = entry.getKey();
                break;
            }
        }

        if (groupToRemove == null)
        {
            throw new RuntimeException("User not found");
        }

        if (adminMap.get(groupToRemove) == user)
        {
            throw new RuntimeException(("Cannot remove admin"));
        }

        // Remove the user from the group
        List<User> usersInGroup = groupUserMap.get(groupToRemove);
        usersInGroup.remove(user);

        // Remove all messages sent by the user from the group's message list
        List<Message> messagesInGroup = groupMessageMap.get(groupToRemove);
        messagesInGroup.removeIf(message -> senderMap.get(message).equals(user));

        // Remove the user's messages from the sender map
        senderMap.entrySet().removeIf(entry -> entry.getValue().equals(user));

        // Calculate the updated total number of messages across all groups
        int totalMessagesAcrossAllGroups = groupMessageMap.values().stream().mapToInt(List::size).sum();

        return usersInGroup.size() + messagesInGroup.size() + totalMessagesAcrossAllGroups;
    }

    public String findMessage(Date start, Date end, int k) throws Exception{
        // Find the Kth latest message between start and end (excluding start and end)
        // If the number of messages between given time is less than K, throw "K is greater than the number of messages" exception
        //your code here
        List<Message> messagesInRange = new ArrayList<>();
        for (Map.Entry<Group, List<Message>> entry : groupMessageMap.entrySet())
        {
            for (Message message : entry.getValue())
            {
                if (message.getTimestamp().after(start) && message.getTimestamp().before(end))
                {
                    messagesInRange.add(message);
                }
            }
        }

        if (messagesInRange.size() < k)
        {
            throw new RuntimeException("K is greater than the number of messages");
        }

        messagesInRange.sort(Comparator.comparing(Message::getTimestamp).reversed());

        return messagesInRange.get(k - 1).getContent();
    }
}