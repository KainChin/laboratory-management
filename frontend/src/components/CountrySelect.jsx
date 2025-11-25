import React, { useState, useRef, useEffect } from "react";
import { ChevronDown } from "lucide-react";
import ReactCountryFlag from "react-country-flag";
import { countries } from "../data/countries";

export default function CountrySelect({ 
  value, 
  onChange, 
  error, 
  disabled = false,
  placeholder = "Select country",
  className = "",
  style = {}
}) {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState("");
  const [highlightedIndex, setHighlightedIndex] = useState(-1);
  const dropdownRef = useRef(null);
  const searchInputRef = useRef(null);
  const listRef = useRef(null);

  // Filter countries based on search term
  const filteredCountries = countries.filter(country =>
    country.name.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Get selected country
  const selectedCountry = countries.find(c => c.name === value);

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setIsOpen(false);
        setSearchTerm("");
        setHighlightedIndex(-1);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  // Focus search input when dropdown opens
  useEffect(() => {
    if (isOpen && searchInputRef.current) {
      searchInputRef.current.focus();
    }
  }, [isOpen]);

  // Scroll to highlighted item
  useEffect(() => {
    if (highlightedIndex >= 0 && listRef.current) {
      const items = listRef.current.children;
      if (items[highlightedIndex]) {
        items[highlightedIndex].scrollIntoView({
          block: "nearest",
          behavior: "smooth"
        });
      }
    }
  }, [highlightedIndex]);

  const handleSelect = (country) => {
    onChange(country.name);
    setIsOpen(false);
    setSearchTerm("");
    setHighlightedIndex(-1);
  };

  const handleKeyDown = (e) => {
    if (!isOpen) {
      if (e.key === "Enter" || e.key === " " || e.key === "ArrowDown") {
        e.preventDefault();
        setIsOpen(true);
      }
      return;
    }

    switch (e.key) {
      case "ArrowDown":
        e.preventDefault();
        setHighlightedIndex(prev => 
          prev < filteredCountries.length - 1 ? prev + 1 : prev
        );
        break;
      case "ArrowUp":
        e.preventDefault();
        setHighlightedIndex(prev => prev > 0 ? prev - 1 : 0);
        break;
      case "Enter":
        e.preventDefault();
        if (highlightedIndex >= 0 && filteredCountries[highlightedIndex]) {
          handleSelect(filteredCountries[highlightedIndex]);
        }
        break;
      case "Escape":
        e.preventDefault();
        setIsOpen(false);
        setSearchTerm("");
        setHighlightedIndex(-1);
        break;
      default:
        break;
    }
  };

  return (
    <div 
      ref={dropdownRef} 
      className={`relative ${className}`}
      style={{ width: "100%", ...style }}
    >
      {/* Selected value display */}
      <button
        type="button"
        onClick={() => !disabled && setIsOpen(!isOpen)}
        onKeyDown={handleKeyDown}
        disabled={disabled}
        className={`w-full min-h-[40px] p-2 border rounded-lg text-sm bg-white flex items-center justify-between transition-all ${
          disabled ? "opacity-60 cursor-not-allowed" : "cursor-pointer hover:border-[#FF5A5A]"
        }`}
        style={{
          borderColor: error ? "#FF5A5A" : "#CCC",
          ...style
        }}
        aria-expanded={isOpen}
        aria-haspopup="listbox"
      >
        <span className={selectedCountry ? "text-black font-medium" : "text-gray-400"}>
          {selectedCountry ? (
            <span style={{ display: "flex", alignItems: "center" }}>
              <ReactCountryFlag
                countryCode={selectedCountry.code}
                svg
                style={{
                  width: "1.5em",
                  height: "1.5em",
                  marginRight: "8px",
                  borderRadius: "2px"
                }}
                title={selectedCountry.name}
              />
              <span>{selectedCountry.name}</span>
            </span>
          ) : (
            placeholder
          )}
        </span>
        <ChevronDown 
          size={20} 
          className={`transition-transform ${isOpen ? "rotate-180" : ""}`}
          style={{ color: "#777" }}
        />
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div 
          className="absolute z-50 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg"
          style={{ 
            maxHeight: "300px", 
            overflow: "hidden",
            animation: "slideDown 0.2s ease-out"
          }}
        >
          <style>{`
            @keyframes slideDown {
              from {
                opacity: 0;
                transform: translateY(-8px);
              }
              to {
                opacity: 1;
                transform: translateY(0);
              }
            }
          `}</style>
          {/* Search input */}
          <div className="p-2 border-b border-gray-200 sticky top-0 bg-white">
            <input
              ref={searchInputRef}
              type="text"
              value={searchTerm}
              onChange={(e) => {
                setSearchTerm(e.target.value);
                setHighlightedIndex(0);
              }}
              onKeyDown={handleKeyDown}
              placeholder="Search country..."
              className="w-full p-2 text-sm border border-gray-300 rounded focus:outline-none focus:border-[#FF5A5A]"
            />
          </div>

          {/* Country list */}
          <ul 
            ref={listRef}
            role="listbox"
            className="overflow-y-auto"
            style={{ 
              maxHeight: "240px",
              scrollbarWidth: "thin",
              scrollbarColor: "#FF5A5A #f3f4f6"
            }}
          >
            {filteredCountries.length > 0 ? (
              filteredCountries.map((country, index) => (
                <li
                  key={country.code}
                  role="option"
                  aria-selected={country.name === value}
                  onClick={() => handleSelect(country)}
                  onMouseEnter={() => setHighlightedIndex(index)}
                  className={`px-3 py-2 cursor-pointer text-sm flex items-center transition-colors ${
                    highlightedIndex === index 
                      ? "bg-[#FFE5E5]" 
                      : country.name === value 
                      ? "bg-[#FFF0F0]" 
                      : "hover:bg-gray-100"
                  }`}
                  style={{
                    fontWeight: country.name === value ? 600 : 400,
                  }}
                >
                  <ReactCountryFlag
                    countryCode={country.code}
                    svg
                    style={{
                      width: "1.5em",
                      height: "1.5em",
                      marginRight: "10px",
                      borderRadius: "2px"
                    }}
                    title={country.name}
                  />
                  <span>{country.name}</span>
                  {country.name === value && (
                    <span style={{ marginLeft: "auto", color: "#FF5A5A", fontSize: "16px" }}>✓</span>
                  )}
                </li>
              ))
            ) : (
              <li className="px-3 py-4 text-sm text-gray-500 text-center">
                No countries found
              </li>
            )}
          </ul>
        </div>
      )}
    </div>
  );
}
