import React from "react";
import { Card } from "antd";
import { motion } from "framer-motion";
import {
  FaFlask,
  FaTools,
  FaChartLine,
  FaUserShield,
} from "react-icons/fa";
import { BsBoxSeamFill } from "react-icons/bs";
import { Link } from "react-router-dom";

// Mock data for allowed services
const mockAllowedServices = [
  "MONITORING_SERVICE",
  "TEST_ORDER_SERVICE",
  "INSTRUMENT_SERVICE",
  "WAREHOUSE_SERVICE",
  "PATIENT_SERVICE",
];

function ServiceWidget() {
  const services = mockAllowedServices;
  if (services.length === 0) return null;
  const authToken = localStorage.getItem("token");

  const serviceMapping = {
    MONITORING_SERVICE: {
      title: "System Monitoring",
      icon: <FaChartLine size={24} />,
      desc: "Tracks the system activities, event logs, and manages system configurations.",
      path: "http://13.238.99.218:3000/monitoring/",
      color: "from-blue-500 to-cyan-500",
    },
    TEST_ORDER_SERVICE: {
      title: "Test Order Management",
      icon: <FaFlask size={24} />,
      desc: "Handles creating, updating, reviewing test orders and related comments.",
      path: "/test-orders",
      color: "from-purple-500 to-pink-500",
    },
    INSTRUMENT_SERVICE: {
      title: "Instrument Management",
      icon: <FaTools size={24} />,
      desc: "Manages laboratory instruments, reagents and executing blood testing.",
      path: "#",
      color: "from-orange-500 to-red-500",
    },
    WAREHOUSE_SERVICE: {
      title: "Warehouse Management",
      icon: <BsBoxSeamFill size={24} />,
      desc: "Manages warehouse inventory including viewing and modifying equipment.",
      path: "http://3.24.171.31:5173/",
      color: "from-green-500 to-emerald-500",
    },
    PATIENT_SERVICE: {
      title: "Patient Management",
      icon: <FaUserShield size={24} />,
      desc: "Handles patient records, appointments, and essential patient-related information.",
      path: "http://54.206.211.154:3000",
      color: "from-indigo-400 to-purple-500",
    },
  };

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      {services.map((service, index) => {
        const serviceConfig = serviceMapping[service];
        return (
          <Link key={index + service} to={serviceConfig.path + (authToken ? "?code=" + authToken : "")}>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1, duration: 0.5 }}
            >
              <Card className="group relative overflow-hidden cursor-pointer transition-all duration-300 hover:-translate-y-2 shadow-[0_4px_20px_-2px_rgba(0,0,0,0.08)] hover:shadow-[0_8px_30px_-4px_rgba(0,0,0,0.15)]
                                  h-full max-h-[300px] border border-muted-foreground/30"
                style={{ border: "1px solid #7777" }}>
                <div
                  className={`absolute inset-0 bg-gradient-to-br ${serviceConfig.color} opacity-0 group-hover:opacity-5 transition-opacity duration-300`}
                />

                <div className="relative p-3 flex flex-col h-full min-h-[220px]">
                  {/* Icon */}
                  <div
                    className={`w-14 h-14 rounded-xl bg-gradient-to-br ${serviceConfig.color} flex items-center justify-center mb-4 shadow-lg group-hover:scale-110 transition-transform duration-300`}
                  >
                    <div className="text-white">{serviceConfig.icon}</div>
                  </div>

                  {/* Title */}
                  <p className="!m-0 !mb-4 text-[18px] font-bold text-black  group-hover:text-[hsl(200,95%,45%)] transition-colors duration-300">
                    {serviceConfig.title}
                  </p>

                  {/* Description */}
                  <p className="text-sm text-[#777777] leading-relaxed flex-grow">
                    {serviceConfig.desc}
                  </p>

                  {/* Hover Arrow */}
                  <div className="mt-4 flex items-center text-[hsl(200,95%,45%)] opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                    <span className="text-sm font-semibold">Explore</span>
                    <svg
                      className="w-5 h-5 ml-1 transform group-hover:translate-x-1 transition-transform duration-300"
                      fill="none"
                      viewBox="0 0 24 24"
                      stroke="currentColor"
                    >
                      <path
                        strokeLinecap="round"
                        strokeLinejoin="round"
                        strokeWidth={2}
                        d="M9 5l7 7-7 7"
                      />
                    </svg>
                  </div>
                </div>
              </Card>
            </motion.div>
          </Link>
        );
      })}
    </div>
  );
}

export default ServiceWidget;
