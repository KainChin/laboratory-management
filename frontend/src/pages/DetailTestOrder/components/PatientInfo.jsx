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
    } else if (!/^(\+\d{1,3}[- ]?)?\d{10}$/.test(formData.phone.replace(/\s/g, ''))) {
      newErrors.phone = "Phone number is invalid. Must be 10 digits with optional country code (e.g., +84 or +1)";
    }
    
    // Validate Email
    if (!formData.email || !formData.email.trim()) {
      newErrors.email = "Email is required";
    } else {
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
      if (!emailRegex.test(formData.email)) {
        newErrors.email = "Invalid email format";
      }
    }
    
    // Validate Gender
    if (!formData.gender) {
      newErrors.gender = "Gender is required";
    }
    
    // Validate Citizen ID
    if (!formData.citizenId || !formData.citizenId.trim()) {
      newErrors.citizenId = "Citizen ID is required";
    }
    
    // Validate Address
    if (!formData.address || !formData.address.trim()) {
      newErrors.address = "Address is required";
    }
    
    // Validate Country
    if (!formData.country || !formData.country.trim()) {
      newErrors.country = "Country is required";
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
  const accent = "#FF5A5A";
  const border = "#CCC";
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
      fontSize: 18,
      fontWeight: 800,
      margin: 0,
      lineHeight: 1,
      letterSpacing: '0.6px',
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
      minHeight: "40px",
      padding: "10px 12px",
      border: "1px solid #CCC",
      borderRadius: 6,
      fontSize: 16,
      fontWeight: 600,
      color: "#000",
      outline: "none",
      transition: "all 0.3s ease",
    },
    select: {
      width: "100%",
      minHeight: "40px",
      padding: "10px 12px",
      border: "1px solid #CCC",
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
      marginTop: 8,
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
            <User size={24} />
          </div>
          <h3 style={styles.title}>Patient Information</h3>
        </div>
        {isEditing && (
          <div style={styles.buttonGroup}>
            <button
              onClick={onCancel}
              style={{
                padding: "10px 16px",
                minHeight: "40px",
                borderRadius: 8,
                border: "1px solid #CCC",
                background: "#fff",
                color: "#777777",
                fontSize: 14,
                fontWeight: 600,
                cursor: "pointer",
                transition: "all 0.2s",
              }}
              onMouseOver={(e) => e.target.style.background = "#f9fafb"}
              onMouseOut={(e) => e.target.style.background = "#fff"}
              aria-label="Cancel editing patient information"
            >
              Cancel
            </button>
            <button
              onClick={handleSubmit}
              style={{
                padding: "10px 16px",
                minHeight: "40px",
                borderRadius: 8,
                border: "1px solid #CCC",
                background: "#FF5A5A",
                color: "#fff",
                fontSize: 14,
                fontWeight: 600,
                cursor: "pointer",
                transition: "all 0.2s",
              }}
              onMouseOver={(e) => e.target.style.background = "#FF3A3A"}
              onMouseOut={(e) => e.target.style.background = "#FF5A5A"}
              aria-label="Save patient information changes"
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
                  id="patient-name-input"
                  value={formData.patientName}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.patientName ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.patientName ? "#FF5A5A" : "#CCC"}
                  aria-label="Patient Name"
                  aria-required="true"
                  aria-invalid={errors.patientName ? "true" : "false"}
                  aria-describedby={errors.patientName ? "patient-name-error" : undefined}
                  placeholder="Enter patient's full name"
                />
                {errors.patientName && (
                  <div 
                    id="patient-name-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 12,
                      marginTop: 8,
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
                  id="gender-select"
                  value={formData.gender}
                  onChange={handleChange}
                  style={{
                    ...styles.select,
                    borderColor: errors.gender ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.gender ? "#FF5A5A" : "#CCC"}
                  aria-label="Gender"
                  aria-required="true"
                  aria-invalid={errors.gender ? "true" : "false"}
                  aria-describedby={errors.gender ? "gender-error" : undefined}
                >
                  <option value="">Select Gender</option>
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
                {errors.gender && (
                  <div 
                    id="gender-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 14,
                      marginTop: 8,
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
                  id="phone-input"
                  value={formData.phone}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.phone ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.phone ? "#FF5A5A" : "#CCC"}
                  aria-label="Phone Number"
                  aria-required="true"
                  aria-invalid={errors.phone ? "true" : "false"}
                  aria-describedby={errors.phone ? "phone-error" : undefined}
                  placeholder="Enter phone number (e.g., +84 123 456 789)"
                />
                {errors.phone && (
                  <div 
                    id="phone-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 14,
                      marginTop: 8,
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
              <div>
                <input
                  type="text"
                  name="address"
                  id="address-input"
                  value={formData.address}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.address ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.address ? "#FF5A5A" : "#CCC"}
                  aria-label="Address"
                  aria-required="true"
                  aria-invalid={errors.address ? "true" : "false"}
                  aria-describedby={errors.address ? "address-error" : undefined}
                  placeholder="Enter full address"
                />
                {errors.address && (
                  <div 
                    id="address-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 12,
                      marginTop: 8,
                      fontWeight: 500
                    }}>
                    {errors.address}
                  </div>
                )}
              </div>
            ) : (
              <div style={styles.pairValue}>{patient.address}</div>
            )}
          </div>

          <div>
            <div style={styles.pairLabel}>Country</div>
            {isEditing ? (
              <div>
                <input
                  type="text"
                  name="country"
                  id="country-input"
                  value={formData.country}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.country ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.country ? "#FF5A5A" : "#CCC"}
                  aria-label="Country"
                  aria-required="true"
                  aria-invalid={errors.country ? "true" : "false"}
                  aria-describedby={errors.country ? "country-error" : undefined}
                  placeholder="Enter country name"
                />
                {errors.country && (
                  <div 
                    id="country-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 12,
                      marginTop: 8,
                      fontWeight: 500
                    }}>
                    {errors.country}
                  </div>
                )}
              </div>
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
                  id="dob-input"
                  value={formData.dateOfBirth}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.dateOfBirth ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.dateOfBirth ? "#FF5A5A" : "#CCC"}
                  aria-label="Date of Birth"
                  aria-required="true"
                  aria-invalid={errors.dateOfBirth ? "true" : "false"}
                  aria-describedby={errors.dateOfBirth ? "dob-error" : undefined}
                />
                {errors.dateOfBirth && (
                  <div 
                    id="dob-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 14,
                      marginTop: 8,
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
                  id="email-input"
                  value={formData.email}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.email ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.email ? "#FF5A5A" : "#CCC"}
                  aria-label="Email Address"
                  aria-required="false"
                  aria-invalid={errors.email ? "true" : "false"}
                  aria-describedby={errors.email ? "email-error" : undefined}
                  placeholder="Enter email address (e.g., patient@example.com)"
                />
                {errors.email && (
                  <div 
                    id="email-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 14,
                      marginTop: 8,
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
              <div>
                <input
                  type="text"
                  name="citizenId"
                  id="citizen-id-input"
                  value={formData.citizenId}
                  onChange={handleChange}
                  style={{
                    ...styles.input,
                    borderColor: errors.citizenId ? "#FF5A5A" : "#CCC"
                  }}
                  onFocus={(e) => e.target.style.borderColor = "#FF5A5A"}
                  onBlur={(e) => e.target.style.borderColor = errors.citizenId ? "#FF5A5A" : "#CCC"}
                  aria-label="Citizen ID"
                  aria-required="true"
                  aria-invalid={errors.citizenId ? "true" : "false"}
                  aria-describedby={errors.citizenId ? "citizen-id-error" : undefined}
                  placeholder="Enter citizen ID or passport number"
                />
                {errors.citizenId && (
                  <div 
                    id="citizen-id-error"
                    role="alert"
                    style={{
                      color: "#FF5A5A",
                      fontSize: 12,
                      marginTop: 8,
                      fontWeight: 500
                    }}>
                    {errors.citizenId}
                  </div>
                )}
              </div>
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
