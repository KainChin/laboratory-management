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
  '<>': 'Significantly different'
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