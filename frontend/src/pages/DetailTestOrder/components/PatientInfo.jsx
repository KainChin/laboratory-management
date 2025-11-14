import React, { useState, useEffect } from "react";
import { User } from "lucide-react";

export default function PatientInfo({ patient = {}, isEditing = false, onSave, onCancel }) {
  const [formData, setFormData] = useState({
    patientName: "",
    dateOfBirth: "",
    citizenId: "",
    country: "",
    gender: "",
    phone: "",
    address: "",
    email: "",
  });

  const [errors, setErrors] = useState({});

  // Helper: Convert dd/MM/yyyy (from backend) to yyyy-MM-dd (for date input)
  const parseBackendDateToInput = (dateStr) => {
    if (!dateStr) return "";
    const parts = dateStr.split("/");
    if (parts.length !== 3) return dateStr;
    const [dd, mm, yyyy] = parts;
    return `${yyyy}-${mm.padStart(2, "0")}-${dd.padStart(2, "0")}`;
  };

  // Cập nhật formData khi patient thay đổi hoặc chuyển sang chế độ edit
  useEffect(() => {
    if (isEditing && patient) {
      const dobValue = patient.dob || patient.dateOfBirth || "";
      setFormData({
        patientName: patient.name || patient.patientName || "",
        dateOfBirth: parseBackendDateToInput(dobValue),
        citizenId: patient.citizenId || "",
        country: patient.country || "",
        gender: patient.gender || "",
        phone: patient.phone || "",
        address: patient.address || "",
        email: patient.email || "",
      });
      setErrors({}); // Reset errors khi bắt đầu edit
    }
  }, [isEditing, patient]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    
    // Clear error khi user bắt đầu sửa field đó
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ""
      }));
    }
  };

  const validateForm = () => {
    const newErrors = {};
    
    // Validate Patient Name
    if (!formData.patientName || !formData.patientName.trim()) {
      newErrors.patientName = "Patient name is required";
    }
    
    // Validate Date of Birth
    if (!formData.dateOfBirth) {
      newErrors.dateOfBirth = "Date of birth is required";
    } else {
      const dobDate = new Date(formData.dateOfBirth);
      const today = new Date();
      dobDate.setHours(0, 0, 0, 0);
      today.setHours(0, 0, 0, 0);
      
      if (dobDate > today) {
        newErrors.dateOfBirth = "Date of Birth cannot be in future";
      }
    }
    
    // Validate Phone
    if (!formData.phone || !formData.phone.trim()) {
      newErrors.phone = "Phone number is required";
    } else if (!/^[0-9()+\-\s]{7,20}$/.test(formData.phone)) {
      newErrors.phone = "Invalid phone number format";
    }
    
    // Validate Email
    if (formData.email && formData.email.trim()) {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        newErrors.email = "Invalid email format";
      }
    }
    
    // Validate Gender
    if (!formData.gender) {
      newErrors.gender = "Gender is required";
    }
    
    return newErrors;
  };

  const handleSubmit = () => {
    // Validate trước khi submit
    const validationErrors = validateForm();
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }
    
    if (onSave) {
      onSave(formData);
    }
  };
  const accent = "#ff6b6b";
  const border = "#e6eef3";
  const muted = "#000000ff";

  const styles = {
    card: {
      border: `1px solid ${border}`,
      borderRadius: 10,
      padding: 20,
      background: "#fff",
      marginBottom: 20,
    },
    header: {
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
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
      background: `${accent}20`,
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
    input: {
      width: "100%",
      padding: "8px 12px",
      border: "1px solid #e5e7eb",
      borderRadius: 6,
      fontSize: 15,
      fontWeight: 600,
      color: "#000",
      outline: "none",
      transition: "all 0.3s ease",
    },
    select: {
      width: "100%",
      padding: "8px 12px",
      border: "1px solid #e5e7eb",
      borderRadius: 6,
      fontSize: 15,
      fontWeight: 600,
      color: "#000",
      outline: "none",
      transition: "all 0.3s ease",
      backgroundColor: "#fff",
    },
    buttonGroup: {
      display: "flex",
      gap: 8,
      marginTop: 4,
    },
  };

  return (
    <section 
      style={{
        ...styles.card,
        transition: "all 0.3s ease",
      }} 
      className="patient-card"
    >
      <div style={styles.header}>
        <div style={styles.headerLeft}>
          <div style={styles.iconSq}>
            <User size={16} />
          </div>
          <h3 style={styles.title}>Patient Information</h3>
        </div>
        {isEditing && (
          <div style={styles.buttonGroup}>
            <button
              onClick={onCancel}
              style={{
                padding: "8px 16px",
                borderRadius: 8,
                border: "1px solid #e5e7eb",
                background: "#fff",
                color: "#777777",
                fontSize: 14,
                fontWeight: 600,
                cursor: "pointer",
                transition: "all 0.2s",
              }}
              onMouseOver={(e) => e.target.style.background = "#f9fafb"}
              onMouseOut={(e) => e.target.style.background = "#fff"}
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              style={{
                padding: "8px 16px",
                borderRadius: 8,
                border: "none",
                background: "#f65f63",
                color: "#fff",
                fontSize: 14,
                fontWeight: 600,
                cursor: "pointer",
                transition: "all 0.2s",
              }}
              onMouseOver={(e) => e.target.style.background = "#e54e54"}
              onMouseOut={(e) => e.target.style.background = "#f65f63"}
            >
              Change
            </button>
          </div>
        )}
      </div>

      <div style={styles.grid} className="patient-grid">
        <div style={styles.col} className="patient-col">
          <div>
            <div style={styles.pairLabel}>Patient Name</div>
            {isEditing ? (
              <div>
                <input
                  type="text"
                  name="patientName"
                  value={formData.patientName}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.patientName ? "#FF0000" : "#e5e7eb"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                  onBlur={(e) => e.target.style.borderColor = errors.patientName ? "#FF0000" : "#e5e7eb"}
                />
                {errors.patientName && (
                  <div style={{
                    color: "#FF0000",
                    fontSize: 12,
                    marginTop: 4,
                    fontWeight: 500
                  }}>
                    {errors.patientName}
                  </div>
                )}
              </div>
            ) : (
              <div style={styles.pairValue}>{patient.name}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Gender</div>
            {isEditing ? (
              <div>
                <select
                  name="gender"
                  value={formData.gender}
                  onChange={handleChange}
                  style={{
                    ...styles.select,
                    borderColor: errors.gender ? "#FF0000" : "#e5e7eb"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                  onBlur={(e) => e.target.style.borderColor = errors.gender ? "#FF0000" : "#e5e7eb"}
                >
                  <option value="">Select Gender</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
                {errors.gender && (
                  <div style={{
                    color: "#FF0000",
                    fontSize: 13,
                    marginTop: 4,
                    fontWeight: 500
                  }}>
                    {errors.gender}
                  </div>
                )}
              </div>
            ) : (
              <div style={styles.pairValue}>{patient.gender}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Phone</div>
            {isEditing ? (
              <div>
                <input
                  type="tel"
                  name="phone"
                  value={formData.phone}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.phone ? "#FF0000" : "#e5e7eb"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                  onBlur={(e) => e.target.style.borderColor = errors.phone ? "#FF0000" : "#e5e7eb"}
                />
                {errors.phone && (
                  <div style={{
                    color: "#FF0000",
                    fontSize: 13,
                    marginTop: 4,
                    fontWeight: 500
                  }}>
                    {errors.phone}
                  </div>
                )}
              </div>
            ) : (
              <div style={styles.pairValue}>{patient.phone}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Address</div>
            {isEditing ? (
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleChange}
                style={styles.input}
                onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
              />
            ) : (
              <div style={styles.pairValue}>{patient.address}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Country</div>
            {isEditing ? (
              <input
                type="text"
                name="country"
                value={formData.country}
                onChange={handleChange}
                style={styles.input}
                onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
              />
            ) : (
              <div style={styles.pairValue}>{patient.country}</div>
            )}
          </div>
        </div>

        <div style={styles.col} className="patient-col">
          <div>
            <div style={styles.pairLabel}>Date of Birth</div>
            {isEditing ? (
              <div>
                <input
                  type="date"
                  name="dateOfBirth"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.dateOfBirth ? "#FF0000" : "#e5e7eb"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                  onBlur={(e) => e.target.style.borderColor = errors.dateOfBirth ? "#FF0000" : "#e5e7eb"}
                />
                {errors.dateOfBirth && (
                  <div style={{
                    color: "#FF0000",
                    fontSize: 13,
                    marginTop: 4,
                    fontWeight: 500
                  }}>
                    {errors.dateOfBirth}
                  </div>
                )}
              </div>
            ) : (
              <div style={styles.pairValue}>{patient.dob}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Email</div>
            {isEditing ? (
              <div>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.email ? "#FF0000" : "#e5e7eb"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                  onBlur={(e) => e.target.style.borderColor = errors.email ? "#FF0000" : "#e5e7eb"}
                />
                {errors.email && (
                  <div style={{
                    color: "#FF0000",
                    fontSize: 13,
                    marginTop: 4,
                    fontWeight: 500
                  }}>
                    {errors.email}
                  </div>
                )}
              </div>
            ) : (
              <div style={styles.pairValue}>{patient.email}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Citizen ID</div>
            {isEditing ? (
              <input
                type="text"
                name="citizenId"
                value={formData.citizenId}
                onChange={handleChange}
                style={styles.input}
                onFocus={(e) => e.target.style.borderColor = "#FF0000"}
                onBlur={(e) => e.target.style.borderColor = "#e5e7eb"}
              />
            ) : (
              <div style={styles.pairValue}>{patient.citizenId}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Age</div>
            <div style={styles.pairValue}>{patient.age}</div>
          </div>

          <div>
            <div style={styles.pairLabel}>Blood Collection ID</div>
            <div style={styles.pairValue}>{patient.bloodCollectionId}</div>
          </div>
        </div>
      </div>
    </section>
  );
}
