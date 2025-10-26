import React from "react";
import { User } from "lucide-react";

export default function PatientInfo({ patient = {} }) {
  return (
    <section className="card patient-card">
      <div className="card-header">
        <div className="card-header-left">
          <div className="icon-sq">
            <User size={14} />
          </div>
          <h3>Patient Information</h3>
        </div>
      </div>

      <div className="patient-grid">
        <div className="patient-col">
          <div className="muted">Patient Name</div>
          <div className="bold">{patient.name}</div>

          <div className="muted">Gender</div>
          <div className="bold">{patient.gender}</div>

          <div className="muted">Phone</div>
          <div className="bold">{patient.phone}</div>

          <div className="muted">Address</div>
          <div className="bold">{patient.address}</div>
        </div>

        <div className="patient-col">
          <div className="muted">Age</div>
          <div className="bold">{patient.age}</div>

          <div className="muted">Date of Birth</div>
          <div className="bold">{patient.dob}</div>

          <div className="muted">Email</div>
          <div className="bold">{patient.email}</div>

          <div className="muted">Citizen ID</div>
          <div className="bold">{patient.citizenId}</div>
        </div>
      </div>
    </section>
  );
}