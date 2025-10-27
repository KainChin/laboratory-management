import React, { useState } from "react";
import { MessageSquare, Edit2, Trash2 } from "lucide-react";

export default function Comments() {
  const [text, setText] = useState("");
  const comments = [
    { author: "Dr. Wilson", body: "Hemoglobin levels slightly below normal range. Recommend follow-up iron studies." },
    { author: "Dr. Wilson", body: "Glucose levels elevated. Patient should be advised about diabetes management." },
  ];

  return (
    <section className="card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <MessageSquare size={14} />
          </div>
          <h3>Comments</h3>
        </div>
      </div>

      <div className="muted small-desc">Comments and notes related to this test order</div>

      <div className="comments-list">
        {comments.map((c, i) => (
          <div className="comment" key={i}>
            <div className="comment-author"><b>{c.author}</b></div>
            <div className="comment-body">{c.body}</div>
            <div className="comment-actions">
              <button className="icon-btn"><Edit2 size={14} /></button>
              <button className="icon-btn"><Trash2 size={14} /></button>
            </div>
          </div>
        ))}
      </div>

      <div className="comment-input">
        <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Enter your comment..." />
        <button className="btn-primary" onClick={() => setText("")}>Add Comment</button>
      </div>
    </section>
  );
}