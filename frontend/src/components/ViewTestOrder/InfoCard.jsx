export default function InfoCard({ title, value, color, icon, border }) {
  return (
    <div
      className={`bg-white border ${border} rounded-xl shadow-sm p-5 flex flex-col justify-center hover:shadow-md transition`}
    >
      <div className="flex items-center justify-between">
        <h3 className={`font-semibold ${color}`}>{title}</h3>
        {icon}
      </div>
      <p className="text-2xl font-bold text-gray-900 mt-1 text-center">{value}</p>
    </div>
  );
}
