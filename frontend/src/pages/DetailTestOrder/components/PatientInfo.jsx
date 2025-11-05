import React from "react";
import { User } from "lucide-react";

export default function PatientInfo({ patient = {} }) {
  const accent = "#ff6b6b";
  const border = "#e6eef3";
  const muted = "#555";

  const styles = {
    card: {
      border: `1px solid ${border}`,
      borderRadius: 10,
      padding: 20,
      background: "#fff",
      marginBottom: 20, // thêm khoảng cách nhỏ giữa 2 bảng
    },
    header: {
      display: "flex",
      alignItems: "center",
      marginBottom: 16,
    },
    headerLeft: {
      display: "flex",
      alignItems: "center",
      gap: 12,
    },
    iconSq: {
      width: 34,
      height: 34,
      borderRadius: 8,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      background: `${accent}20`, // light tint
      color: accent,
      flex: "none",
    },
    title: {
      color: accent,
      fontSize: 20,
      fontWeight: 700,
      margin: 0,
      lineHeight: 1,
    },
    grid: {
      display: "grid",
      gridTemplateColumns: "1fr 1fr",
      gap: 8,
      alignItems: "start",
    },
    col: {
      display: "flex",
      flexDirection: "column",
      gap: 12,
    },
    pairLabel: {
      color: muted,
      fontSize: 14,
      marginBottom: 2,
      fontWeight: 500,
    },
    pairValue: {
      fontWeight: 700,
      fontSize: 16,
      color: "#000",
    },
  };

  return (
    <section style={styles.card} className="patient-card">
      <div style={styles.header}>
        <div style={styles.headerLeft}>
          <div style={styles.iconSq}>
            <User size={16} />
          </div>
          <h3 style={styles.title}>Patient Information</h3>
        </div>
      </div>

      <div style={styles.grid} className="patient-grid">
        <div style={styles.col} className="patient-col">
          <div>
            <div style={styles.pairLabel}>Patient Name</div>
            <div style={styles.pairValue}>{patient.name}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Gender</div>
            <div style={styles.pairValue}>{patient.gender}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Phone</div>
            <div style={styles.pairValue}>{patient.phone}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Address</div>
            <div style={styles.pairValue}>{patient.address}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Blood Collection ID</div>
            <div style={styles.pairValue}>{patient.bloodCollectionId}</div>
          </div>
        </div>

        <div style={styles.col} className="patient-col">
          <div>
            <div style={styles.pairLabel}>Age</div>
            <div style={styles.pairValue}>{patient.age}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Date of Birth</div>
            <div style={styles.pairValue}>{patient.dob}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Email</div>
            <div style={styles.pairValue}>{patient.email}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Citizen ID</div>
            <div style={styles.pairValue}>{patient.citizenId}</div>
          </div>
        </div>
      </div>
    </section>
  );
}
