export const FLAG_MEANINGS = {
  'N': 'Normal',
  'L': 'Low',
  'H': 'High',
  'A': 'Abnormal',
  'AA': 'Very abnormal',
  'LL': 'Below low alert',
  'HH': 'Above high alert',
  '<': 'Below absolute low-off scale',
  '>': 'Above absolute high-off scale',
  'I': 'Intermediate',
  'MS': 'Moderately sensitive',
  'VS': 'Very sensitive',
  'B': 'Better',
  'W': 'Worse',
  'D': 'Significant change down',
  'U': 'Significant change up',
  'S': 'Susceptible',
  'R': 'Resistant',
  'P': 'Positive',
  'neg': 'Negative', // Using 'neg' since 'N' is already used for Normal
  '<>': 'Significantly different',
  'OTHER': 'Other'
};

export function getFlagMeaning(flag) {
  if (!flag) return 'No flag';
  const upperFlag = flag.toString().toUpperCase();
  return FLAG_MEANINGS[upperFlag] || upperFlag;
}

export function calculateTestResultStats(testResults) {
  if (!testResults || !testResults.testResultParameter || !Array.isArray(testResults.testResultParameter) || testResults.testResultParameter.length === 0) {
    return {
      total: 0,
      flagCounts: {},
      hasResults: false
    };
  }

  const flagCounts = {};
  testResults.testResultParameter.forEach(param => {
    if (!param) return;
    // Default to 'N' if no flag is present
    const flag = param.flag ? param.flag.toString().toUpperCase() : 'N';
    flagCounts[flag] = (flagCounts[flag] || 0) + 1;
  });

  return {
    total: testResults.testResultParameter.length,
    flagCounts,
    hasResults: true
  };
}

// Color mapping for flags in charts
export function getFlagColor(flag) {
  if (!flag) return '#9ca3af'; // grey for no flag
  
  const upperFlag = flag.toString().toUpperCase();
  
  // Known flag colors matching the CSS
  const colorMap = {
    'N': '#10b981',  // green for Normal
    'H': '#f65f63',  // red for High
    'HH': '#dc2626', // darker red
    'L': '#f59e0b',  // orange for Low
    'LL': '#d97706', // darker orange
    '>': '#f65f63',  // red (high)
    '<': '#f59e0b',  // orange (low)
    'A': '#f97316',  // orange for Abnormal
    'AA': '#dc2626', // dark red
    'VS': '#8b5cf6', // purple
    'MS': '#a855f7', // lighter purple
    'I': '#3b82f6',  // blue for Intermediate
    'B': '#10b981',  // green for Better
    'W': '#f65f63',  // red for Worse
    'D': '#f59e0b',  // orange for down
    'U': '#f65f63',  // red for up
    'S': '#06b6d4',  // cyan for Susceptible
    'R': '#f59e0b',  // orange for Resistant
    'P': '#10b981',  // green for Positive
    'NEG': '#9ca3af', // grey for Negative
    '<>': '#6366f1',  // indigo
    'OTHER': '#9ca3af', // grey for Other
  };
  
  return colorMap[upperFlag] || '#9ca3af'; // default grey for unknown flags
}