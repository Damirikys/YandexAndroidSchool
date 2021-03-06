package ru.urfu.taskmanager.task_manager.models;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;

import ru.urfu.taskmanager.utils.tools.ISO8601;

public class TaskEntry implements Parcelable
{
    private int mId;
    private int mComplete;

    private String title;
    private String description;
    private String ttl;
    private String created;
    private String edited;
    private String color;
    private String imageUrl;

    public TaskEntry() {
    }

    public TaskEntry(int id) {
        this.mId = id;
    }

    private TaskEntry(Parcel in) {
        title = in.readString();
        description = in.readString();
        ttl = in.readString();
        color = in.readString();
        mComplete = in.readInt();
    }

    public static final Creator<TaskEntry> CREATOR = new Creator<TaskEntry>() {
        @Override
        public TaskEntry createFromParcel(Parcel in) {
            return new TaskEntry(in);
        }

        @Override
        public TaskEntry[] newArray(int size) {
            return new TaskEntry[size];
        }
    };

    public TaskEntry setId(int id) {
        this.mId = id;
        return this;
    }

    public int getId() {
        return mId;
    }

    public String getTitle() {
        return title;
    }

    public TaskEntry setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public TaskEntry setDescription(String description) {
        this.description = description;
        return this;
    }

    public TaskEntry setImageUrl(String url) {
        this.imageUrl = url;
        return this;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTtl() {
        return ttl;
    }

    public long getTtlTimestamp() {
        try {
            return ISO8601.toTimestamp(ttl);
        } catch (ParseException e) {
            e.printStackTrace();
            return Long.MIN_VALUE;
        }
    }

    public TaskEntry setTtl(long ttl) {
        this.ttl = ISO8601.fromTimestamp(ttl);
        return this;
    }

    public TaskEntry setCreated(long created) {
        this.created = ISO8601.fromTimestamp(created);
        return this;
    }

    public String getCreated() {
        return created;
    }

    public long getCreatedTimestamp() {
        try {
            return ISO8601.toTimestamp(created);
        } catch (Exception e) {
            e.printStackTrace();
            return Long.MIN_VALUE;
        }
    }

    public TaskEntry setEdited(long edited) {
        this.edited = ISO8601.fromTimestamp(edited);
        return this;
    }

    public String getEdited() {
        return edited;
    }

    public long getEditedTimestamp() {
        try {
            return ISO8601.toTimestamp(edited);
        } catch (ParseException e) {
            e.printStackTrace();
            return Long.MIN_VALUE;
        }
    }

    public String getColor() {
        return color;
    }

    public int getColorInt() {
        return Color.parseColor(color);
    }

    public TaskEntry setColor(int color) {
        this.color = String.format("#%06X", (0xFFFFFF & color));
        return this;
    }

    public boolean isCompleted() {
        return (mComplete == 1);
    }

    public String getCompleted() {
        return String.valueOf(mComplete);
    }

    public TaskEntry setCompleted(boolean bool) {
        mComplete = (bool) ? 1 : 0;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 5;

        hash = 89 * hash + (title != null ? title.hashCode() : 0);
        hash = 89 * hash + (description != null ? description.hashCode() : 0);
        hash = 89 * hash + color.hashCode();

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        try {
            TaskEntry other = (TaskEntry) obj;
            return this.mId == other.mId;
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int describeContents() {
        return CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mId);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(ttl);
        dest.writeString(color);
        dest.writeInt(mComplete);
    }

    @Override
    public String toString() {
        return "[" + "title: " + title + "; " + "description: " + description + "; " +
                "ttl: " + ttl + "; " + "color: " + color + "]";
    }
}
